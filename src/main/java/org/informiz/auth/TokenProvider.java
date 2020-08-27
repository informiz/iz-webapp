package org.informiz.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.security.auth.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.*;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    public static final int MINUTE_IN_MILLIS = 60000;

    private int tokenExpiration = 24 * 60 * 60;

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
                    .build(); // Automatically verifies expiration
        }
        return instance;
    }

    public String createToken(Authentication authentication) {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();
        String email = user.getAttributes().get("email").toString();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiration);

        List<String> scopes = new ArrayList<>();
        authentication.getAuthorities().forEach(authority -> scopes.add(authority.getAuthority()));

        // TODO: no entity-id for ROLE_VIEWER? Random? email hash-code?
        String entityId = authentication.getAuthorities().stream()
                .filter(authority -> InformizGrantedAuthority.class.isInstance(authority))
                .findFirst().map(auth -> ((InformizGrantedAuthority)auth).getEntityId()).orElse(null);

        return JWT.create()
                .withSubject(user.getName())
                .withIssuer(tokenIssuer)
                .withAudience(tokenAudience)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("scopes", scopes)
                .withClaim("eid", entityId)
                .withClaim("email", email)
                .sign(HMAC512(tokenSecret.getBytes()));
    }

    public String getUserNameFromToken(String token) {

        return JWT.require(HMAC512(tokenSecret))
                .build()
                .verify(token)
                .getSubject();
        }

    public DecodedJWT validateToken(String token) {
        try {
            DecodedJWT decodedJWT = getVerifier().verify(token);
            // TODO: verify subject?
            return decodedJWT;
        } catch (JWTVerificationException exception){
            logger.info("Invalid token: " + token, exception);
        }
        return null;
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
}