package org.informiz;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
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
    private int maxUploadSizeInMb = 10 * 1024 * 1024; // 10 MB

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

    //Tomcat large file upload connection reset
    //http://www.mkyong.com/spring/spring-file-upload-and-connection-reset-issue/
    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        return new TomcatServletWebServerFactory() {
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                connector.setMaxPostSize(maxUploadSizeInMb);
                connector.setMaxSavePostSize(maxUploadSizeInMb);
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {

                    ((AbstractHttp11Protocol <?>) connector.getProtocolHandler()).setMaxSwallowSize(maxUploadSizeInMb);
                    logger.info("Set MaxSwallowSize "+ maxUploadSizeInMb);
                }
            }
        };

    }

    public static void main(String[] args) {
		SpringApplication.run(InformizWebApp.class, args);
	}

}
