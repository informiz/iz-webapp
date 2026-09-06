package org.informiz.conf;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.CookieUtils;
import org.informiz.auth.TokenSecurityContextRepository;
import org.informiz.model.InformizEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.UUID;

import static org.informiz.auth.CookieUtils.TOKEN_MAX_AGE;


@Configuration
@EnableWebSecurity
@ComponentScan("org.informiz.auth")
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleAuthClientId;

    private final TokenSecurityContextRepository securityContextRepo;

    private final CookieUtils cookieUtils;

    @Autowired
    public SecurityConfig(TokenSecurityContextRepository securityContextRepo, CookieUtils cookieUtils) {
        this.securityContextRepo = securityContextRepo;
        this.cookieUtils = cookieUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RequestCache nullRequestCache = new NullRequestCache();
        CookieCsrfTokenRepository repo = cookieUtils.csrfTokenRepo();
        List<GrantedAuthority> minAuth = AuthUtils.anonymousAuthorities();
        PathPatternRequestMatcher.Builder patternBuilder = PathPatternRequestMatcher.withDefaults();

        http
                .sessionManagement((session) -> session
                        .requireExplicitAuthenticationStrategy(true))
                .requestCache((cache) -> cache
                        .requestCache(nullRequestCache))
                .securityContext((context) -> context.requireExplicitSave(true)
                        .securityContextRepository(securityContextRepo))
                .redirectToHttps(configurer -> {})
                .anonymous(configurer ->
                        configurer.principal("viewer").authorities(minAuth))
                .csrf(configurer -> configurer
                        .ignoringRequestMatchers(patternBuilder.matcher("/oauth/login")) // csrf token sent in Google cookie/param
                        .csrfTokenRepository(repo))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(patternBuilder.matcher("/oauth/login"),
                            patternBuilder.matcher("/oauth/logout")).permitAll();
                    auth.anyRequest().hasRole("VIEWER");
                })

        ;
        return http.build();
    }

    @Bean(name = "googleOAuthService")
    ClientIdService googleOAuthService() {
        return () -> googleAuthClientId;
    }

    @Bean
    public LayoutDialect layoutDialect() {
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

        @Autowired
        private CookieUtils cookieUtils;

        public boolean isOwner(DefaultOAuth2User principal, InformizEntity entity) {
            return principal.getName().equals(entity.getOwnerId());
        }

        public String getDisabled(DefaultOAuth2User principal, InformizEntity entity) {
            return isOwner(principal, entity) ? "false" : "true";
        }

        public String getNonce() {
            Cookie cookie =  cookieUtils.getCookie(request, cookieUtils.nonceCookieName());
            if (cookie == null) {
                cookie = cookieUtils.setNonceCookie(response, TOKEN_MAX_AGE,
                        UUID.randomUUID().toString().substring(0, 16));
            }

            return cookie.getValue();
        }
    }

    @Bean(name = "sUtils")
    static SecUtils sUtilsBean() {
        return new SecUtils();
    }

    // TODO: best-practice way to allow h2-console access locally?
/*
    private static DefaultHttpSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultHttpSecurityExpressionHandler handler = new DefaultHttpSecurityExpressionHandler();
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
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**"));
    }
*/
}