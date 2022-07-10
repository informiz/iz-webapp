package org.informiz.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static final int TOKEN_MAX_AGE = 24 * 60 * 60 * 1000; // 1 day in miliseconds
    public static final String JWT_COOKIE_NAME = "iz_jwt";
    public static final String NONCE_COOKIE_NAME = "iz_nonce";
    public static final String GOOGLE_STATE_COOKIE_NAME = "g_state";

    public static void setCookie(HttpServletResponse response, String cookieName, int age, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
