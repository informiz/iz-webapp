package org.informiz.ctrl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.util.Strings;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.CookieUtils;
import org.informiz.auth.TokenProvider;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
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
    private final FactCheckerRepository factCheckerRepo;

    private final TokenProvider tokenProvider;

    private final SecurityContextRepository securityContextRepo;

    private static final Logger logger = LoggerFactory.getLogger(AccessRestController.class);

    @Autowired
    public AccessRestController(FactCheckerRepository factCheckerRepo, TokenProvider tokenProvider, SecurityContextRepository securityContextRepo) {
        this.factCheckerRepo = factCheckerRepo;
        this.tokenProvider = tokenProvider;
        this.securityContextRepo = securityContextRepo;
    }


    /**
     * Log-in a user. Receive a Google ID-token from the browser and grant access accordingly.
     * This authorization process is protected by CSRF and nonce tokens.
     *
     * @param referer      the page that invoked login
     * @param nonce        the expected nonce value
     * @param gCsrf        the expected Google CSRF token value
     * @param response     current HTTP response
     * @param request      current HTTP request
     * @param credential   Google ID-token
     * @param g_csrf_token Google CSRF token
     * @throws IOException in case of unexpected error while redirecting
     */
    @PostMapping(path = LOGIN_PATH, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void login(@RequestHeader("referer") Optional<String> referer,
                      @CookieValue(name = NONCE_COOKIE_NAME) String nonce,
                      @CookieValue(name = GOOGLE_CSRF_COOKIE_NAME) String gCsrf,
                      HttpServletResponse response,
                      HttpServletRequest request,
                      String credential,
                      String g_csrf_token) throws IOException {

        Assert.isTrue(Objects.equals(g_csrf_token, gCsrf), "Wrong CSRF value");
        try {
            GoogleIdToken.Payload idToken = getIdToken(credential, nonce);

            String email = idToken.getEmail();
            String subscriber = idToken.getSubject();

            // TODO: user may not be a member, get entity-id from ES
            FactCheckerBase checker = factCheckerRepo.findByEmail(email);
            String entityId = checker == null ? "" : checker.getEntityId();
            Collection<? extends GrantedAuthority> authorities = AuthUtils.getUserAuthorities(email, entityId);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("eid", entityId);
            attributes.put("gid", subscriber);
            attributes.put("name", entityId);

            OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(new DefaultOAuth2User(authorities, attributes, "name"),
                    authorities, clientId);
            CookieUtils.setCookie(response, JWT_COOKIE_NAME, TOKEN_MAX_AGE, tokenProvider.createToken(auth));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepo.saveContext(context, request, response);

        } catch (Exception e) {
            logger.error("Unexpected error during auth", e);
            // TODO: revoke other cookies? reset g_state cookie?
            CookieUtils.setCookie(response, JWT_COOKIE_NAME, 0, "");
        }
        String path = referer.isPresent() ? new URL(referer.get()).getPath() : "/";
        response.sendRedirect(path);
    }

    private GoogleIdToken.Payload getIdToken(final String credential, @NotBlank String nonce) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), // TODO: use client certificate from truststore for biTLS?
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId)).build();
        GoogleIdToken.Payload idToken = verifier.verify(credential).getPayload();
        Assert.isTrue(Strings.isNotEmpty(idToken.getNonce()), "Missing nonce from ID-token");
        Assert.isTrue(Objects.equals(idToken.getNonce(), nonce), "Wrong nonce value");

        return idToken;
    }

    @PostMapping(path = LOGOUT_PATH)
    public void logout(HttpServletResponse response, @RequestHeader("referer") Optional<String> referer) throws IOException {
        CookieUtils.setCookie(response, JWT_COOKIE_NAME, 0, "");
        CookieUtils.setCookie(response, GOOGLE_STATE_COOKIE_NAME, 0, "");

        // TODO: stay on same page if it doesn't require auth?
        // String path  = referer.isPresent() ? new URL(referer.get()).getPath() : "/";
        response.sendRedirect("/");
    }

}
