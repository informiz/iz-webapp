package org.informiz.conf;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.TokenSecurityContextRepository;
import org.informiz.model.ChainCodeEntity;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

import static org.informiz.auth.InformizGrantedAuthority.*;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    TokenSecurityContextRepository securityContextRepo;

    @Autowired
    CookieCsrfTokenRepository csrfTokenRepository;

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
                .csrfTokenRepository(csrfTokenRepository)
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
    public CookieCsrfTokenRepository csrfTokenRepo() {
        CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
        // repo.setCookieDomain(cookieDomain); // TODO: add spring property
        repo.setParameterName("iz_csrf");
        repo.setCookieName("IZ_CSRF_TOKEN");
        repo.setParameterName("IZ_CSRF_TOKEN");
        return repo;
    }


    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(String.format("%s > %s > %s > %s",
                ROLE_ADMIN, ROLE_MEMBER, ROLE_CHECKER, ROLE_VIEWER));
        return roleHierarchy;
    }

    public static class SecUtils {

        public static boolean isOwner(DefaultOAuth2User principal, ChainCodeEntity entity) {
            return entity.getOwnerId() == principal.getAttributes().get("eid");
        }
    }

    @Bean(name = "sUtils")
    public SecUtils sUtilsBean() {
        return new SecUtils();
    }

}