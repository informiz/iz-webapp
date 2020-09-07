package org.informiz.repo.checker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.nio.charset.Charset;

@Profile({"dev"})
@Component
public class DbInit {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    private void postConstruct() {
        try {
            Resource script = new ClassPathResource("initDb.sql", this.getClass().getClassLoader());
            EncodedResource encodedScript = new EncodedResource(script, Charset.forName("utf8"));
            ScriptUtils.executeSqlScript(dataSource.getConnection(), encodedScript);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }
}