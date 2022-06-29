package org.informiz.conf;

import nz.net.ultraq.thymeleaf.LayoutDialect;
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
                //.and() // TODO: anonymous config doesn't seem to be working..? Setting default-auth in securityContextRepo instead
                //.anonymous().principal("viewer").authorities(AuthUtils.anonymousAuthorities())
                .and()
                .csrf().csrfTokenRepository(new CookieCsrfTokenRepository())
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/login").permitAll()
                .antMatchers("/oauth/logout").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/style/**").permitAll()
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