package org.informiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 @EnableTransactionManagement
 */
@SpringBootApplication(scanBasePackages = "org.informiz")
@ServletComponentScan
@EnableJpaRepositories("org.informiz.repo")
@EntityScan("org.informiz.model")
public class InformizWebApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
		SpringApplication.run(InformizWebApp.class, args);
	}

}
