package org.informiz.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class InformizOAuth2RequestRedirectFilter extends OncePerRequestFilter {

    @Autowired
    private CookieRequestCache cache;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = httpServletRequest.getRequestURI();
        // TODO: is there protection against DoS attacks?
        if (requestUri != null && requestUri.equals("/oauth2/authorization/google")) {
            cache.saveRequest(httpServletRequest, httpServletResponse);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
        // TODO: remove cached request cookie after login
    }
}
