package org.informiz.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CookieRequestCache implements RequestCache {
    public static final String CACHE_REQUEST_COOKIE_NAME = "cached_request";
    private static final int cookieExpireSeconds = 60;


    // TODO: get config from https://cloud.spring.io/spring-cloud-consul/reference/html/
    // TODO: key for each channel
    @Value("${iz.webapp.key-ring-id}")
    private String keyRingId;

    @Value("${iz.webapp.key-id}")
    private String keyId;

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        String redirectUrl = getRedirectUrl(request);
        return (redirectUrl == null) ? null : CachedRequest.fromString(redirectUrl);
    }

    private String getRedirectUrl(HttpServletRequest request) {
        Cookie authReqCookie = WebUtils.getCookie(request, CACHE_REQUEST_COOKIE_NAME);
        if (authReqCookie != null) {
            try {
                return AuthUtils.decrypt(authReqCookie.getValue(), keyRingId, keyId);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to deserialize cached request", e);
            }
        }
        return null;
    }

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        int age;
        String value;

        // TODO: find a way to pass previous url, as this is always the login page
        String redirectUrl = request.getHeader("referer");
        if (redirectUrl == null) return;

        try {
            // TODO: is it necessary to encrypt it?
            value = AuthUtils.encrypt(redirectUrl, keyRingId, keyId);
            age = cookieExpireSeconds;
        } catch (IOException e) {
            // TODO: log
            throw new IllegalStateException("Failed to cache request", e);
        }
        CookieUtils.setLaxCookie(response, CACHE_REQUEST_COOKIE_NAME, age, value);
    }

    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        String redirectUrl = getRedirectUrl(request);
        HttpServletRequest matchingRequest = null;
        if (redirectUrl != null) {
            try {
                matchingRequest = new MatchingRequest(request, new URL(redirectUrl).getPath());
            } catch (MalformedURLException e) {}
        }
        return matchingRequest;
    }

    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.setLaxCookie(response, CACHE_REQUEST_COOKIE_NAME, 0, "");
    }


    public static class CachedRequest implements SavedRequest {

        private String redirectUrl;


        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        @Override
        public String getRedirectUrl() {
            return redirectUrl;
        }

        public String toString() {
            return redirectUrl;
        }

        public static CachedRequest fromString(String redirectUrl) {
            CachedRequest request = new CachedRequest();
            request.setRedirectUrl(redirectUrl);
            return request;
        }

        @Override
        public List<Cookie> getCookies() {
            unsupportedMethodCalled("getCookies");
            return null;
        }

        @Override
        public String getMethod() {
            unsupportedMethodCalled("getMethod");
            return null;
        }

        @Override
        public List<String> getHeaderValues(String name) {
            unsupportedMethodCalled("getHeaderValues");
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            unsupportedMethodCalled("getHeaderNames");
            return null;
        }

        @Override
        public List<Locale> getLocales() {
            unsupportedMethodCalled("getLocales");
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            unsupportedMethodCalled("getParameterValues");
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            unsupportedMethodCalled("getParameterMap");
            return null;
        }

        private static void unsupportedMethodCalled(String method) {
            throw new IllegalStateException(String.format("Did not expect %s to be called", method));
       }
    }

    public static class MatchingRequest extends HttpServletRequestWrapper {

        private String requestUri;

        public MatchingRequest(HttpServletRequest request, String uri) {
            super(request);
            this.requestUri = uri;
        }

        @Override
        public String getRequestURI() {
            return requestUri;
        }
    }
}