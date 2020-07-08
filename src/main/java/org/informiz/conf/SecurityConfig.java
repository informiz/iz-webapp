package org.informiz.conf;

import org.informiz.auth.AuthUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    ObjectFactory<HttpSession> httpSessionFactory;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .antMatchers("/", "/public/**", "/style*", "/home.html", "/error*").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userAuthoritiesMapper(this.userAuthoritiesMapper())
                .and()
                .defaultSuccessUrl("/factchecker/", true)
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/home.html")
                .permitAll()
                .and().csrf().disable();
    }

    @Bean(name = "googleOAuthService")
    public ClientIdService googleOAuthService() {
        return () -> googleAuthClientId;
    }

    public interface ClientIdService {
        String getClientId();
    }


    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oidcUserAuthority = (OAuth2UserAuthority) authority;
                    String email = oidcUserAuthority.getAttributes().get("email").toString();
                    AuthUtils.getUserAuthorities(email).forEach(auth -> mappedAuthorities.add(auth));
                }
                mappedAuthorities.add(authority);
            });

            return mappedAuthorities;
        };
    }
}
