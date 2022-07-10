package org.informiz.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.informiz.auth.CookieUtils.*;

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
        Cookie cookie = WebUtils.getCookie(holder.getRequest(), JWT_COOKIE_NAME);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
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

        // Not authenticated - set nonce if necessary
        cookie = WebUtils.getCookie(holder.getRequest(), NONCE_COOKIE_NAME);
        if (cookie == null) {
            CookieUtils.setCookie(holder.getResponse(), NONCE_COOKIE_NAME, TOKEN_MAX_AGE,
                    UUID.randomUUID().toString().substring(0, 16));
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