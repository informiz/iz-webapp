package org.informiz.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;


import static org.informiz.auth.CookieUtils.JWT_COOKIE_NAME;

/**
 * A cookie-based security context repository
 */
@Component
public class TokenSecurityContextRepository implements SecurityContextRepository {

    @Autowired
    private TokenProvider tokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(TokenSecurityContextRepository.class);

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder holder) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Cookie cookie = WebUtils.getCookie(holder.getRequest(), JWT_COOKIE_NAME);
        if (cookie != null) {
            try {
                 context.setAuthentication(tokenProvider.authFromToken(cookie.getValue()));
                return context;
            } catch (JWTVerificationException ex) {
                // anonymous
            }
        } else {
            // anonymous
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // Login endpoint sets the cookie
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        return cookie != null; // TODO: any issues if the token is invalid/expired?
    }

}