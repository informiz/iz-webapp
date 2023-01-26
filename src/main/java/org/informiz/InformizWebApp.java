package org.informiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "org.informiz")
public class InformizWebApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
		SpringApplication.run(InformizWebApp.class, args);
	}

}
