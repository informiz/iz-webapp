package org.informiz.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * A cookie-based security context repository
 */
@Component
public class TokenSecurityContextRepository implements SecurityContextRepository {

    private final TokenProvider tokenProvider;
    private final CookieUtils cookieUtils;

    private static final Logger logger = LoggerFactory.getLogger(TokenSecurityContextRepository.class);

    // TODO: don't need @Autowired annotation on c'tor?
    public TokenSecurityContextRepository(TokenProvider tokenProvider, CookieUtils cookieUtils) {
        this.tokenProvider = tokenProvider;
        this.cookieUtils = cookieUtils;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder holder) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Cookie cookie = cookieUtils.getSessionCookie(holder.getRequest());
        if (cookie != null) {
            try {
                context.setAuthentication(tokenProvider.authFromToken(cookie.getValue()));
                return context;
            } catch (JWTVerificationException ex) {
                // bad token, not authenticated
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
        Cookie cookie = cookieUtils.getSessionCookie(request);
        return cookie != null; // TODO: any issues if the token is invalid/expired?
    }
}