package org.informiz.conf;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.informiz.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.informiz.auth.CookieRequestCache.CACHE_REQUEST_COOKIE_NAME;
import static org.informiz.auth.CookieUtils.JWT_COOKIE_NAME;
import static org.informiz.auth.InformizGrantedAuthority.*;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    InformizLoginSuccessHandler loginSuccessHandler;

    @Autowired
    CookieAuthRequestRepository authRequestRepo;

    @Autowired
    CookieRequestCache requestCache;

    @Autowired
    TokenSecurityContextRepository securityContextRepo;

    @Autowired
    InformizAuthMapper userAuthoritiesMapper;

    @Autowired
    InformizOAuth2RequestRedirectFilter cachedRequestFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .securityContext().securityContextRepository(securityContextRepo)
                .and()
                .requiresChannel().anyRequest().requiresSecure()
                .and()
                .anonymous().authorities(AuthUtils.anonymousAuthorities())
                .and()
                .csrf()
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET).hasRole("VIEWER")
                .antMatchers(HttpMethod.HEAD).hasRole("VIEWER")
                .antMatchers(HttpMethod.OPTIONS).hasRole("VIEWER")
                .antMatchers(HttpMethod.TRACE).hasRole("VIEWER")
                .anyRequest().authenticated()
                .and()
                .logout()
                .deleteCookies(JWT_COOKIE_NAME)
                .and()
                .addFilterBefore(cachedRequestFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .requestCache().requestCache(requestCache)
                .and()
                .oauth2Login()
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler())
                .authorizationEndpoint().authorizationRequestRepository(authRequestRepo)
                .and()
                .userInfoEndpoint().userAuthoritiesMapper(userAuthoritiesMapper)
        ;
    }

    @Bean(name = "googleOAuthService")
    public ClientIdService googleOAuthService() {
        return () -> googleAuthClientId;
    }

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    public interface ClientIdService {
        String getClientId();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(String.format("%s > %s > %s > %s",
                ROLE_ADMIN, ROLE_MEMBER, ROLE_CHECKER, ROLE_VIEWER));
        return roleHierarchy;
    }

    @Bean
    AuthenticationFailureHandler loginFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                CookieUtils.setCookie(response, JWT_COOKIE_NAME, 0, "");
                CookieUtils.setCookie(response, CACHE_REQUEST_COOKIE_NAME, 0, "");
            }
        };
    }
}
