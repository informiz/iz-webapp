package org.informiz.auth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CookieAuthRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_REQUEST_COOKIE_NAME = "oauth2_request";
    private static final int cookieExpireSeconds = 60;

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(OAuth2AuthorizationRequest.class, new OauthRequestJsonSerializer());
        module.addDeserializer(OAuth2AuthorizationRequest.class, new OauthRequestJsonDeserializer());
        mapper.registerModule(module);
    }

    // TODO: get config from https://cloud.spring.io/spring-cloud-consul/reference/html/
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
        CookieUtils.setLaxCookie(response, OAUTH2_REQUEST_COOKIE_NAME, age, value);
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


    public static class OauthRequestJsonSerializer extends JsonSerializer<OAuth2AuthorizationRequest> {

        private static ObjectMapper hashMapWithObjectsMapper = new ObjectMapper();
        static {
            /**
             * IMPORTANT: This configuration introduces a security vulnerability.
             * To minimize the risk, the strings provided by this class MUST be encrypted.
             * @see AuthUtils#encrypt(String, String, String)
             * @see AuthUtils#decrypt(String, String, String)
             */
            hashMapWithObjectsMapper.activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
            }

        @Override
        public void serialize(OAuth2AuthorizationRequest authRequest, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException,
                JsonProcessingException {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("authorizationUri", authRequest.getAuthorizationUri());
            jsonGenerator.writeStringField("authorizationGrantType", authRequest.getGrantType().getValue());
            jsonGenerator.writeStringField("responseType", authRequest.getResponseType().getValue());
            jsonGenerator.writeStringField("clientId", authRequest.getClientId());
            jsonGenerator.writeStringField("redirectUri", authRequest.getRedirectUri());
            jsonGenerator.writeStringField("scopes", String.join(", ", authRequest.getScopes()));
            jsonGenerator.writeStringField("state", authRequest.getState());
            jsonGenerator.writeStringField("authorizationRequestUri", authRequest.getAuthorizationRequestUri());
            String params = hashMapWithObjectsMapper.writeValueAsString(authRequest.getAdditionalParameters());
            jsonGenerator.writeStringField("additionalParameters", params);
            String attrs = hashMapWithObjectsMapper.writeValueAsString(authRequest.getAttributes());
            jsonGenerator.writeStringField("attributes", attrs);
            jsonGenerator.writeEndObject();

        }
    }

    public static class OauthRequestJsonDeserializer extends JsonDeserializer<OAuth2AuthorizationRequest> {

        @Override
        public OAuth2AuthorizationRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            OAuth2Request request = jsonParser.readValueAs(OAuth2Request.class);
            AuthorizationGrantType grantType = request.getAuthorizationGrantTypeObject();
            OAuth2AuthorizationRequest.Builder builder;

            if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType)) {
                builder = OAuth2AuthorizationRequest.authorizationCode();
            } else {
                // TODO: need to support refresh-token?
                throw new IllegalStateException(String.format("Unsupported grant type: %s", grantType.getValue()));
            }

            return builder
                    .attributes(request.getAttributesMap())
                    .redirectUri(request.getRedirectUri())
                    .additionalParameters(request.getAdditionalParametersMap())
                    .authorizationRequestUri(request.getAuthorizationRequestUri())
                    .authorizationUri(request.getAuthorizationUri())
                    .clientId(request.getClientId())
                    .scopes(request.getScopesSet())
                    .state(request.getState())
                    .build();
        }
    }



    public static class OAuth2Request {
        private String authorizationUri;
        private String authorizationGrantType;
        private String responseType;
        private String clientId;
        private String redirectUri;
        private String scopes;
        private String state;
        private String additionalParameters;
        private String authorizationRequestUri;
        private String attributes;

        private static ObjectMapper hashMapWithObjectsMapper = new ObjectMapper();
        static {
            /**
             * IMPORTANT: This configuration introduces a security vulnerability.
             * To minimize the risk, classes calling this service MUST use encrypted strings.
             * @see AuthUtils#encrypt(String, String, String)
             * @see AuthUtils#decrypt(String, String, String)
             */
            hashMapWithObjectsMapper.activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        }

        static TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {};


        public String getAuthorizationUri() {
            return authorizationUri;
        }

        public void setAuthorizationUri(String authorizationUri) {
            this.authorizationUri = authorizationUri;
        }


        public AuthorizationGrantType getAuthorizationGrantTypeObject() {
            return new AuthorizationGrantType(authorizationGrantType);
        }


        public String getAuthorizationGrantType() {
            return authorizationGrantType;
        }

        public void setAuthorizationGrantType(String authorizationGrantType) {
            this.authorizationGrantType = authorizationGrantType;
        }


        public OAuth2AuthorizationResponseType getResponseTypeObject() {
            if (OAuth2AuthorizationResponseType.CODE.getValue().equals(authorizationGrantType))
                return OAuth2AuthorizationResponseType.CODE;
            else if (OAuth2AuthorizationResponseType.TOKEN.getValue().equals(authorizationGrantType))
                return OAuth2AuthorizationResponseType.TOKEN;
            throw new IllegalStateException(String.format("Authorization response type %s is neither CODE nor TOKEN",
                    authorizationGrantType));
        }


        public String getResponseType() {
            return responseType;
        }

        public void setResponseType(String responseType) {
            this.responseType = responseType;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getScopes() {
            return scopes;
        }

        public void setScopes(String scopes) {
            this.scopes = scopes;
        }

        public Set<String> getScopesSet() {
            return Stream.of( scopes.trim().split("\\s*,\\s*") ).collect( Collectors.toSet() );
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getAdditionalParameters() {
            return additionalParameters;
        }

        public void setAdditionalParameters(String additionalParameters) {
            this.additionalParameters = additionalParameters;
        }

        public Map<String, Object> getAdditionalParametersMap() throws JsonProcessingException {
            return hashMapWithObjectsMapper.readValue(additionalParameters, typeRef);
        }

        public String getAuthorizationRequestUri() {
            return authorizationRequestUri;
        }

        public void setAuthorizationRequestUri(String authorizationRequestUri) {
            this.authorizationRequestUri = authorizationRequestUri;
        }

        public String getAttributes() {
            return attributes;
        }

        public void setAttributes(String attributes) {
            this.attributes = attributes;
        }

        public Map<String, Object> getAttributesMap() throws JsonProcessingException {
            return hashMapWithObjectsMapper.readValue(attributes, typeRef);
        }

    }

}