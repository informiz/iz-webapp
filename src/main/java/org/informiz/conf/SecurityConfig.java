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
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.util.WebUtils;

import java.util.UUID;

import static org.informiz.auth.CookieUtils.NONCE_COOKIE_NAME;
import static org.informiz.auth.CookieUtils.TOKEN_MAX_AGE;
import static org.informiz.auth.InformizGrantedAuthority.*;


@Configuration
@EnableWebSecurity
@ComponentScan("org.informiz.auth")
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    private final TokenSecurityContextRepository securityContextRepo;

    @Autowired
    public SecurityConfig(TokenSecurityContextRepository securityContextRepo) {
        this.securityContextRepo = securityContextRepo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .securityContext().securityContextRepository(securityContextRepo)
                .and()
                .requiresChannel().anyRequest().requiresSecure()
                .and()
                .anonymous().principal("viewer").authorities(AuthUtils.anonymousAuthorities())
                .and()
                .csrf()
                .csrfTokenRepository(csrfTokenRepo())
                .and()
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

    private CookieCsrfTokenRepository csrfTokenRepo() {
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

    public static class SecUtils {
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
    public SecUtils sUtilsBean() {
        return new SecUtils();
    }


    @Bean
    @Profile({"dev"})
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .ignoring()
                .requestMatchers("/h2-console/**");
    }

}