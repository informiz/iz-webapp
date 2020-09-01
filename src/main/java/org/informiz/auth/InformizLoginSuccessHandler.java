package org.informiz.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.informiz.auth.CookieUtils.JWT_COOKIE_NAME;
import static org.informiz.auth.TokenProvider.TOKEN_MAX_AGE;

@Component
public class InformizLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CookieRequestCache cache;


    public InformizLoginSuccessHandler() {
        super();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                           HttpServletResponse response, Authentication auth)
            throws ServletException, IOException {
        if (auth != null && (auth instanceof OAuth2AuthenticationToken)) {
            CookieUtils.setCookie(response, JWT_COOKIE_NAME, TOKEN_MAX_AGE, tokenProvider.createToken(auth));
        }
        setRequestCache(cache);
        super.onAuthenticationSuccess(request, response, auth);
    }
}
