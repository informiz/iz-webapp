package org.informiz.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    public static final int TOKEN_MAX_AGE = 24 * 60 * 60 * 1000; // 1 day in miliseconds

    // TODO: secret, issuer, audience per channel
    @Value("${iz.webapp.token.secret}")
    private String tokenSecret;

    @Value("${iz.webapp.token.issuer}")
    private String tokenIssuer;

    @Value("${iz.webapp.token.audience}")
    private String tokenAudience;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    private static JWTVerifier instance;

    public JWTVerifier getVerifier() {
        // Lazy init to make sure @Values are available, doesn't matter if initialized more than once
        if (instance == null) {
            instance  = JWT.require(HMAC512(tokenSecret))
                    .withIssuer(tokenIssuer)
                    .withAudience(tokenAudience)
                    // TODO: verify subject is channel name
                    .build(); // Automatically verifies expiration
        }
        return instance;
    }

    public String createToken(@NotNull Authentication authentication) {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String email = (String) user.getAttributes().get("email");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TOKEN_MAX_AGE);

        List<String> scopes = new ArrayList<>();
        authentication.getAuthorities().forEach(authority -> scopes.add(authority.getAuthority()));

        String entityId = authentication.getAuthorities().stream()
                .filter(authority -> InformizGrantedAuthority.class.isInstance(authority))
                .findFirst().map(auth -> ((InformizGrantedAuthority)auth).getEntityId()).orElse(null);

        return JWT.create()
                // TODO: what to use as subject? How to verify?
                .withSubject(user.getName())
                .withIssuer(tokenIssuer)
                .withAudience(tokenAudience)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("scopes", scopes)
                .withClaim("eid", entityId)
                .withClaim("email", email) // TODO: REMOVE
                .sign(HMAC512(tokenSecret.getBytes()));
    }

    public String getUserNameFromToken(String token) {

        return JWT.require(HMAC512(tokenSecret))
                .build()
                .verify(token)
                .getSubject();
        }

    public DecodedJWT validateToken(String token) {
        return getVerifier().verify(token);
    }

    public OAuth2AuthenticationToken authFromToken(String token) {
        return authFromToken(getVerifier().verify(token));
    }

    // TODO: get rid of email in the code
    public OAuth2AuthenticationToken authFromToken(DecodedJWT jwt) {
        String entityId = jwt.getClaim("eid").asString();
        String email = jwt.getClaim("email").asString();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("eid", entityId);
        attributes.put("email", email);
        attributes.put("name", jwt.getSubject());
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        jwt.getClaim("scopes").asList(String.class).forEach(scope -> {
            authorities.add(new InformizGrantedAuthority(scope, entityId, email));
        });

        OAuth2User user = new DefaultOAuth2User(authorities, attributes, "name");
        return new OAuth2AuthenticationToken(user, authorities, clientId);
    }

    public static DecodedJWT decodeJWT(@NotBlank String token) {
        return JWT.decode(token);
    }

    /**
     * TODO: need additional info in the access-token?
     *
     public class CustomTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
    final Map<String, Object> additionalInfo = new HashMap<>();

    additionalInfo.put("foo", "bar");
    additionalInfo.put("bar", u"baz");

    ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

    return accessToken;
    }

    }
     */

}