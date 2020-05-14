package org.informiz.conf;

import org.informiz.repo.CryptoUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .antMatchers("/", "/public/**", "/style*", "/home.html", "/login*", "/error*").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                //.loginPage("/login.html")
                .defaultSuccessUrl("/factchecker/", true)
                //.failureUrl("/login-error.html")
                //.permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/home.html")
                .permitAll()
                .and().csrf().disable();
    }

/*
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        auth.inMemoryAuthentication()
                .withUser("user")
                .password(encoder.encode("password"))
                .roles("USER");
    }
*/

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Authentication auth = securityContext.getAuthentication();
                if (auth != null) {
                    String loggedUsername = auth.getName();

                    // TODO: create in-memory wallet for user with private-key and certificate from encrypted storage
                    try {
                        se.getSession().setAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR,
                                // TODO: load network and chaincode names from properties
                                CryptoUtils.createChaincodeProxy("mynetwork", "informiz"));
                        // CryptoUtils.createChaincodeProxy(WALLET, "mynetwork", "informiz"));
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to create chaincode proxy for user", e);
                    }
                }
            }
        };
    }
}
