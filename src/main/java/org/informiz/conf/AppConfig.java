package org.informiz.conf;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("org.informiz.repo")
@EntityScan("org.informiz.model")
public class AppConfig {
}
