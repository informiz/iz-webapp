package org.informiz.conf;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.CookieUtils;
import org.informiz.auth.TokenSecurityContextRepository;
import org.informiz.model.InformizEntity;
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import static org.informiz.auth.CookieUtils.NONCE_COOKIE_NAME;
import static org.informiz.auth.CookieUtils.TOKEN_MAX_AGE;
import static org.informiz.auth.InformizGrantedAuthority.*;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    TokenSecurityContextRepository securityContextRepo;

    @Override
    public void configure(HttpSecurity http) throws Exception {

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
                .authorizeRequests()
                .antMatchers("/oauth/login", "/oauth/logout").permitAll()
                .antMatchers(HttpMethod.GET).hasRole("VIEWER")
                .antMatchers(HttpMethod.HEAD).hasRole("VIEWER")
                .antMatchers(HttpMethod.OPTIONS).hasRole("VIEWER")
                .antMatchers(HttpMethod.TRACE).hasRole("VIEWER")
                .anyRequest().authenticated()
        ;
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

}