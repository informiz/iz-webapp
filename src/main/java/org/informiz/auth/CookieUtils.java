package org.informiz.auth;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

/**
 * Cookie-related utility functions.
 * All cookies are subdomain-specific, no cookies are set for the parent domain (i.e. informiz.org).
 * The full name of informiz cookies is a concatenation of the channel's name and the cookie name. For example:
 * The session cookie for the demo-channel is "demo-channel_iz_jwt".
 * Note that it is not mandatory to have different names for the channel-specific cookies, but it probably makes it
 * easier to troubleshoot on the client side.
 *
 * None of the informiz cookies can be sent cross-origin. They are confined to their own subdomain of informiz.org
 * and the browser is instructed not to send them when e.g. following a link to the channel from a different
 * website (another channel, a link on social-media, etc.).
 */
@Service
public class CookieUtils {

    public static final int TOKEN_MAX_AGE = 24 * 60 * 60; // 1 day in seconds
    public static final int TOKEN_MAX_AGE_MILI = TOKEN_MAX_AGE * 1000;
    private static final String JWT_COOKIE_NAME = "iz_jwt";
    private static final String NONCE_COOKIE_NAME = "iz_nonce";
    private static final String CSRF_COOKIE_NAME = "iz_csrf_token";

    /**
     * TODO: Migrate to FedCM to remove use of third-party cookies. Including the g_state cookie??
     * @apiNote Current documentation says: "g_state stores user sign-out status and is set when using the
     * One Tap popup or Automatic sign-in"
     * @see <a href="https://developers.google.com/identity/gsi/web/guides/fedcm-migration">Migrate to FedCM</a>
     */
    public static final String GOOGLE_STATE_COOKIE_NAME = "g_state";
    public static final String GOOGLE_CSRF_COOKIE_NAME = "g_csrf_token";

    @Value("${iz.channel.id}")
    private String channelId;

    @Value("${iz.channel.name}")
    private String channelName;

    public Cookie setCookie(HttpServletResponse response, String cookieName, int age, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain(channelId);
        cookie.setPath("/");
        response.addCookie(cookie);
        return cookie;
    }

    public Cookie getCookie(HttpServletRequest request, String name) {
        return WebUtils.getCookie(request, name);
    }

    public Cookie setSessionCookie(HttpServletResponse response, int age, String value) {
        return setCookie(response, sessionCookieName(), age, value);
    }

    public Cookie getSessionCookie(HttpServletRequest request) {
        return getCookie(request, sessionCookieName());
    }
    private String sessionCookieName() {
        return String.format("%s_%s", JWT_COOKIE_NAME, channelName);
    }

    public Cookie setNonceCookie(HttpServletResponse response, int age, String value) {
        return setCookie(response, nonceCookieName(), age, value);
    }

    public String getNonce(HttpServletRequest request) {
        Cookie cookie = getCookie(request, nonceCookieName());
        return cookie != null ? cookie.getValue() : "";
    }

    public String nonceCookieName() {
        return String.format("%s_%s", NONCE_COOKIE_NAME, channelName);
    }

    /**
     * Create and configure a (cookie) repository for CSRF tokens.
     *
     * Sets the domain to the channel's domain-name: makes the browser only send the cookie to current domain,
     * not to any parent/subdomains.
     *
     * Sets SameSite to Strict: browser will not send the cookie when e.g. following a link to the channel from a
     * different website (or even a different channel, as it's a different subdomain of informiz.org)
     *
     * @return the configured repository
     */
    public CookieCsrfTokenRepository csrfTokenRepo() {
        CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
        repo.setCookieName(csrfCookieName());
        repo.setParameterName(csrfCookieName());
        repo.setCookieCustomizer(responseCookieBuilder ->
                responseCookieBuilder.domain(channelId)
                        .secure(true)
                        .httpOnly(true)
                        .sameSite("strict"));
        return repo;
    }

    private String csrfCookieName() {
        return String.format("%s_%s", CSRF_COOKIE_NAME, channelName);
    }

}
