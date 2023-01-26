package org.informiz;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_VIEWER;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockSecurityContextFactory.class)
public @interface WithCustomAuth {
    String[] role() default {ROLE_VIEWER};
}
