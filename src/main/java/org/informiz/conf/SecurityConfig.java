package org.informiz.conf;

import org.informiz.repo.CryptoUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpSession;
import java.util.Collection;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    ObjectFactory<HttpSession> httpSessionFactory;

    /*
        @Autowired
        AuthManager authManager;


        @Autowired
        private IzOAuth2AuthProvider authProvider;


        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authProvider);
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {

            auth.parentAuthenticationManager(authManager);
        }

        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return authManager;
        }

    */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .antMatchers("/", "/public/**", "/style*", "/home.html", "/login*", "/error*").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
/*
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                        Authentication authentication) {
                        authentication.setAuthenticated(false);

                    }
                })
*/
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

    @Bean(name = "googleOAuthService")
    public ClientIdService googleOAuthService() {
        return () -> googleAuthClientId;
    }

    public interface ClientIdService {
        String getClientId();
    }


    @EventListener
    public void authSuccessEventListener(AuthenticationSuccessEvent event){

        String email = ((DefaultOAuth2User)event.getAuthentication().getPrincipal()).getAttribute("email");
        Collection<? extends GrantedAuthority> authorities = event.getAuthentication().getAuthorities();

        try {
            HttpSession userSession = httpSessionFactory.getObject(); // Should always have a session
            if (userSession.getAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR) == null) {
                // TODO: get wallet from encrypted storage based on user email address

                userSession.setAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR,
                        // TODO: how to get current network and chaincode ids?
                        CryptoUtils.createChaincodeProxy("mynetwork", "informiz"));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create chaincode proxy for user", e);
        }
    }

}
