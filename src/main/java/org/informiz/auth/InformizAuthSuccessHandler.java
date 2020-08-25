package org.informiz.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class InformizAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static int JWT_EXPIRY = 60 * 60 * 24;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CookieAuthRequestRepository oauth2Repo;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.error("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        String token = tokenProvider.createToken(authentication);
        CookieUtils.setCookie(response, CookieUtils.JWT_COOKIE_NAME, JWT_EXPIRY, token);
        oauth2Repo.removeAuthorizationRequest(request, response);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OAuth2AuthorizationRequest oauth2Request = oauth2Repo.loadAuthorizationRequest(request);
        Optional<String> redirectUri = oauth2Request == null ? Optional.empty() :
                Optional.of(oauth2Request.getRedirectUri());

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new AuthenticationServiceException("Not authorized to access " + redirectUri.get());
        }

        return redirectUri.orElse(getDefaultTargetUrl());
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        // TODO: verify same domain
        return true;
    }
}
