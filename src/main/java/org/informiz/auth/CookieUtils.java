package org.informiz.auth;

import org.springframework.http.ResponseCookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static String JWT_COOKIE_NAME = "jwt";

    public static void setLaxCookie(HttpServletResponse response, String cookieName, int age, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void setCookie(HttpServletResponse response, String cookieName, int age, String value) {
        setCookie(response, cookieName, age, value, "/");
    }

    public static void setCookie(HttpServletResponse response, String cookieName, int age, String value, String path) {
        // TODO: is this not working?
        // ResponseCookie cookie = createCookie(cookieName, age, value, path);
        // Javax Cookie doesn't support same-site config, setting header directly
        //response.addHeader("Set-Cookie", cookie.toString());

        setLaxCookie(response, cookieName, age, value);
    }

    public static ResponseCookie createCookie(String cookieName, int age, String value, String path) {
        return ResponseCookie.from(cookieName, value)
                .maxAge(age)
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .path(path)
                .build();
    }
}
