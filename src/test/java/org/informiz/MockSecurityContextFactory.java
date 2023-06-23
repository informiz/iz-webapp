package org.informiz;

import org.informiz.auth.InformizGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MockSecurityContextFactory implements WithSecurityContextFactory<WithCustomAuth> {

    public static final String DEFAULT_TEST_CHECKER_ID = "usr1234";

    @Override
    public SecurityContext createSecurityContext(WithCustomAuth withCustomAuth) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("eid", DEFAULT_TEST_CHECKER_ID);
        attributes.put("name", DEFAULT_TEST_CHECKER_ID);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role: withCustomAuth.role())
            authorities.add(new InformizGrantedAuthority(role, "1234"));

        OAuth2User user = new DefaultOAuth2User(authorities, attributes, "name");
        Authentication auth = new OAuth2AuthenticationToken(user, authorities, "mockClientId");

        context.setAuthentication(auth);
        return context;
    }
}
