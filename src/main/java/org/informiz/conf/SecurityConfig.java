package org.informiz.conf;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.CookieUtils;
import org.informiz.auth.InformizGrantedAuthority;
import org.informiz.auth.TokenSecurityContextRepository;
import org.informiz.model.InformizEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.WebUtils;

import java.util.List;
import java.util.UUID;

import static org.informiz.auth.CookieUtils.NONCE_COOKIE_NAME;
import static org.informiz.auth.CookieUtils.TOKEN_MAX_AGE;


@Configuration
@EnableWebSecurity
@ComponentScan("org.informiz.auth")
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleAuthClientId;

    private final TokenSecurityContextRepository securityContextRepo;

    public SecurityConfig(TokenSecurityContextRepository securityContextRepo) {
        this.securityContextRepo = securityContextRepo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RequestCache nullRequestCache = new NullRequestCache();
        CookieCsrfTokenRepository repo = csrfTokenRepo();
        List<GrantedAuthority> minAuth = AuthUtils.anonymousAuthorities();

        http
                .sessionManagement((session) -> session
                        .requireExplicitAuthenticationStrategy(true))
                .requestCache((cache) -> cache
                        .requestCache(nullRequestCache))
                .securityContext((context) -> context.requireExplicitSave(true)
                        .securityContextRepository(securityContextRepo))
                .requiresChannel(registry ->
                        registry.anyRequest().requiresSecure())
                .anonymous(configurer ->
                        configurer.principal("viewer").authorities(minAuth))
                .csrf(configurer -> configurer
                        .csrfTokenRepository(repo))
                .authorizeHttpRequests()
                .requestMatchers("/oauth/login", "/oauth/logout").permitAll()
                .requestMatchers(HttpMethod.GET).hasRole("VIEWER")
                .requestMatchers(HttpMethod.HEAD).hasRole("VIEWER")
                .requestMatchers(HttpMethod.OPTIONS).hasRole("VIEWER")
                .requestMatchers(HttpMethod.TRACE).hasRole("VIEWER")
                .anyRequest().authenticated()
        ;
        return http.build();
    }

    private static CookieCsrfTokenRepository csrfTokenRepo() {
        CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
        // repo.setCookieDomain(cookieDomain); // TODO: add spring property
        repo.setParameterName("iz_csrf");
        repo.setCookieName("IZ_CSRF_TOKEN");
        repo.setParameterName("IZ_CSRF_TOKEN");
        repo.setSecure(true);
        repo.setCookieHttpOnly(true);
        return repo;
    }

    @Bean(name = "googleOAuthService")
    ClientIdService googleOAuthService() {
        return () -> googleAuthClientId;
    }

    @Bean
    static LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    public interface ClientIdService {
        String getClientId();
    }

    private static class SecUtils {
        @Autowired
        private HttpServletRequest request;
        @Autowired
        private HttpServletResponse response;

        public boolean isOwner(DefaultOAuth2User principal, InformizEntity entity) {
            return principal.getName().equals(entity.getOwnerId());
        }

        public String getDisabled(DefaultOAuth2User principal, InformizEntity entity) {
            return isOwner(principal, entity) ? "false" : "true";
        }

        public String getNonce() {
            Cookie cookie =  WebUtils.getCookie(request, NONCE_COOKIE_NAME);
            if (cookie == null) {
                cookie = CookieUtils.setCookie(response, NONCE_COOKIE_NAME, TOKEN_MAX_AGE,
                        UUID.randomUUID().toString().substring(0, 16));
            }

            return cookie.getValue();
        }
    }

    @Bean(name = "sUtils")
    static SecUtils sUtilsBean() {
        return new SecUtils();
    }

    // TODO: can't expose as bean, overriding is disabled. Check how to best provide role-hierarchy to spring web-sec
    private static DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(InformizGrantedAuthority.roleHierarchy());
        return handler;
    }


    @Bean
    @Profile("!dev")
    static WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .expressionHandler(webSecurityExpressionHandler());
    }

    @Bean
    @Profile({"dev"})
    @Qualifier("webSecurityCustomizer")
    static WebSecurityCustomizer webSecurityCustomizerDev() {
        return (web) -> web
                .expressionHandler(webSecurityExpressionHandler())
                .ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
    }
}