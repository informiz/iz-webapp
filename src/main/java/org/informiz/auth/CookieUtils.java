package org.informiz.auth;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static final int TOKEN_MAX_AGE = 24 * 60 * 60; // 1 day in seconds
    public static final int TOKEN_MAX_AGE_MILI = TOKEN_MAX_AGE * 1000;
    public static final String JWT_COOKIE_NAME = "iz_jwt";
    public static final String NONCE_COOKIE_NAME = "iz_nonce";
    public static final String CSRF_COOKIE_NAME = "IZ_CSRF_TOKEN";
    public static final String GOOGLE_STATE_COOKIE_NAME = "g_state";
    public static final String GOOGLE_CSRF_COOKIE_NAME = "g_csrf_token";

    public static Cookie setCookie(HttpServletResponse response, String cookieName, int age, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        return cookie;
    }
}
