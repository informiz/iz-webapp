package org.informiz.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static String JWT_COOKIE_NAME = "IZ_AUTH_TOKEN";

    public static void setCookie(HttpServletResponse response, String cookieName, int age, String value) {
        Cookie cookie = createCookie(cookieName, age, value);
        response.addCookie(cookie);
    }

    public static Cookie createCookie(String cookieName, int age, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setPath("/");  // By default - same domain policy
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
