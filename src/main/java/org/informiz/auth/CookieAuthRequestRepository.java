package org.informiz.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CookieAuthRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_REQUEST_COOKIE_NAME = "oauth2_request";
    private static final int cookieExpireSeconds = 60;

    private static ObjectMapper mapper = new ObjectMapper();

    // TODO: key for each channel
    @Value("${iz.webapp.key-ring-id}")
    private String keyRingId;

    @Value("${iz.webapp.key-id}")
    private String keyId;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie authReqCookie = WebUtils.getCookie(request, OAUTH2_REQUEST_COOKIE_NAME);
        if (authReqCookie != null) {
            try {
                String cookieValue = AuthUtils.decrypt(authReqCookie.getValue(), keyRingId, keyId);
                return mapper.readValue(cookieValue, OAuth2AuthorizationRequest.class);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to deserialize auth request", e);
            }
        }

        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        int age = 0;
        String value = "";
        if (authorizationRequest != null) {
            try {
                value = AuthUtils.encrypt(mapper.writeValueAsString(authorizationRequest), keyRingId, keyId);
                age = cookieExpireSeconds;
            } catch (IOException e) {
                // TODO: log
                throw new IllegalStateException("Failed to serialize auth request", e);
            }
        }
        CookieUtils.setCookie(response, OAUTH2_REQUEST_COOKIE_NAME, age, value);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        this.saveAuthorizationRequest(null, request, response);
        return authRequest;
    }

    @Override
    @Deprecated
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return this.loadAuthorizationRequest(request);
    }
}