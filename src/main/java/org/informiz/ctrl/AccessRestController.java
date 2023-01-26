package org.informiz.ctrl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.CookieUtils;
import org.informiz.auth.TokenProvider;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import static org.informiz.auth.CookieUtils.*;

@RestController
@RequestMapping(path = AccessRestController.PREFIX)
public class AccessRestController {

    public static final String PREFIX = "/oauth";

    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    // TODO: user may not be a member, get entity-id from ES
    @Autowired
    private FactCheckerRepository factCheckerRepo;

    @Autowired
    private TokenProvider tokenProvider;


    /**
     * Log-in a user. Receive a Google ID-token from the browser and grant access accordingly.
     * This authorization process is protected by CSRF and nonce tokens.
     * @hidden TODO: Will we gain security by using the auth-code flow? Or use redirect instead of popup (no browser but also no CSRF)?
     * @hidden https://developers.google.com/identity/protocols/oauth2/web-server
     *
     * @param referer the page that invoked login
     * @param nonce the expected nonce value
     * @param response HTTP response to set cookies on
     * @param credential Google ID-token
     * @throws IOException
     */
    @PostMapping(path = LOGIN_PATH, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}) //
    public void login(@RequestHeader("referer") Optional<String> referer,
                        @CookieValue(name = NONCE_COOKIE_NAME) String nonce,
                        HttpServletResponse response,
                        String credential) throws IOException {
        try {
            GoogleIdToken.Payload idToken = getIdToken(credential, nonce);

            String email = idToken.getEmail();
            String subscriber = idToken.getSubject();

            // TODO: user may not be a member, get entity-id from ES
            FactCheckerBase checker = factCheckerRepo.findByEmail(email);
            String entityId = checker == null ? "" : checker.getEntityId();
            Collection<GrantedAuthority> authorities = AuthUtils.getUserAuthorities(email, entityId);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("eid", entityId);
            attributes.put("gid", subscriber);
            attributes.put("name", entityId);

            OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(new DefaultOAuth2User(authorities, attributes, "name"),
                authorities, clientId);
            CookieUtils.setCookie(response, JWT_COOKIE_NAME, TOKEN_MAX_AGE, tokenProvider.createToken(auth));
            CookieUtils.setCookie(response, NONCE_COOKIE_NAME, 0, "");

        } catch (Exception e) {
            // TODO: bad credentials? Log this
            // TODO: revoke other cookies? reset g_state cookie?
            CookieUtils.setCookie(response, JWT_COOKIE_NAME, 0, "");
        }
        response.sendRedirect(referer.orElse("/"));
    }

    private GoogleIdToken.Payload getIdToken(final String credential, @NotBlank String nonce) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), // TODO: right transport? No client certificate
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId)).build();
        GoogleIdToken.Payload idToken = verifier.verify(credential).getPayload();
        Assert.isTrue(Strings.isNotEmpty(idToken.getNonce()), "Missing nonce from ID-token");
        Assert.isTrue(idToken.getNonce().equals(nonce), "Wrong nonce value");

        return idToken;
    }

    @PostMapping(path = LOGOUT_PATH)
    public void logout(HttpServletResponse response, @RequestHeader("referer") Optional<String> referer) throws IOException {
        CookieUtils.setCookie(response, JWT_COOKIE_NAME, 0, "");
        CookieUtils.setCookie(response, GOOGLE_STATE_COOKIE_NAME, 0, "");

        response.sendRedirect(referer.orElse("/"));
    }

}
