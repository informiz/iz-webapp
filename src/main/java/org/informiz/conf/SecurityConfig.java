package org.informiz.conf;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.InformizGrantedAuthority;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleAuthClientId;

    @Autowired
    ObjectFactory<HttpSession> httpSessionFactory;

    @Autowired
    private FactCheckerRepository factCheckerRepo;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and().
                requiresChannel()
                .anyRequest().requiresSecure().and()
                .authorizeRequests() // TODO: allow anonymous (not logged-in?) users
                .antMatchers("/", "/public/**", "/style*", "/home.html", "/error*").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userAuthoritiesMapper(this.userAuthoritiesMapper());
                http.logout()
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
        // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************
        factCheckerRepo.save(new FactCheckerBase("Albert", "ashiagborayi@gmail.com", "https://www.linkedin.com/in/albert-ayi-ashiagbor-a0233815a/"));
        factCheckerRepo.save(new FactCheckerBase("Daniel", "danosaf291@gmail.com", "https://www.linkedin.com/in/daniel-osarfo-8b21a482/"));
        factCheckerRepo.save(new FactCheckerBase("Richard", "richardtm905@gmail.com", "https://www.linkedin.com/in/niraamit/"));
        factCheckerRepo.save(new FactCheckerBase("Kim", "kimberly@informiz.org", "https://www.linkedin.com/in/kimberly-caesar-bb204340/"));
        factCheckerRepo.save(new FactCheckerBase("Nira", "nira@informiz.org", "https://www.linkedin.com/in/niraamit/"));

        InformizGrantedAuthority.setCheckers(factCheckerRepo.findAll());
        // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************

        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oidcUserAuthority = (OAuth2UserAuthority) authority;
                    String email = oidcUserAuthority.getAttributes().get("email").toString();
                    AuthUtils.getUserAuthorities(email,
                            factCheckerRepo.findByEmail(email).getEntityId())
                            .forEach(auth -> mappedAuthorities.add(auth));
                }
                mappedAuthorities.add(authority);
            });

            return mappedAuthorities;
        };
    }

    @Value("${server.ssl.key-store}")
    private Resource keyStore;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${server.ssl.trust-store}")
    private Resource trustStore;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    private RestTemplate getRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(
                            keyStore.getFile(),
                            keyStorePassword.toCharArray(),
                            keyStorePassword.toCharArray())
                    .loadTrustMaterial(
                            trustStore.getURL(),
                            keyStorePassword.toCharArray(),
// TODO: ******************************** DEVELOPING, REMOVE THIS!! ********************************
                            new TrustSelfSignedStrategy())
                    .build();

            HttpClient httpClient = HttpClients.custom()
// TODO: ************************ DEVELOPING, REMOVE NoopHostnameVerifier!! ************************
                    // use NoopHostnameVerifier with caution, see https://stackoverflow.com/a/22901289/3890673
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
