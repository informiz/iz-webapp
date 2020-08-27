package org.informiz.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CookieAuthRequestRepository authRequestRepository;

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Cookie cookie = WebUtils.getCookie(request, CookieUtils.JWT_COOKIE_NAME);
            if (cookie == null) {
                SecurityContextHolder.getContext().setAuthentication(AuthUtils.anonymousAuthToken());
            } else {
                // TODO: why does this fail for Anonymous?
                SecurityContextHolder.getContext().setAuthentication(tokenProvider.authFromToken(cookie.getValue()));
            }
        } catch (JWTVerificationException ex) {
            SecurityContextHolder.getContext().setAuthentication(AuthUtils.anonymousAuthToken());
        } catch (Exception ex) {
            logger.warn("Could not set user authentication in security context", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}