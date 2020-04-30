package org.informiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import java.util.EnumSet;

/**
 @EnableTransactionManagement
 */
@SpringBootApplication(scanBasePackages = "org.informiz")
@EnableAutoConfiguration
@ServletComponentScan
@EnableJpaRepositories("org.informiz.repo")
@EntityScan("org.informiz.model")
public class InformizWebApp extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        // Session-id in cookie, not in URL
        servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        // servletContext.getSessionCookieConfig().setSecure(true);   //TODO: only send over HTTPS
        servletContext.getSessionCookieConfig().setHttpOnly(true); // no access from js
        // TODO: better configure here or in application.properties? I.e:
        // server.servlet.session.cookie.http-only=true
        // server.servlet.session.cookie.secure=true

    }

    public static void main(String[] args) {
		SpringApplication.run(InformizWebApp.class, args);
	}

}
