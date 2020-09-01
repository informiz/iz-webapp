package org.informiz.auth;

import org.informiz.model.FactCheckerBase;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;

public class InformizGrantedAuthority implements GrantedAuthority {

    public static final String ROLE_VIEWER = "ROLE_VIEWER";
    public static final String ROLE_CHECKER = "ROLE_CHECKER";
    public static final String ROLE_MEMBER = "ROLE_MEMBER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // The fact-checker's role
    private String role;

    // The fact-checker's entity-id
    private String entityId;


    // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************
    private String email;

    private static Map<String, String> checkersMap = new HashMap<>();

    public static void setCheckers(Iterable<FactCheckerBase> checkers) {
        checkers.forEach(checker -> checkersMap.put(checker.getEmail(), checker.getEntityId()));
    }

    public String getEntityId() {
        return checkersMap.get(email);
    }
    // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************


    public InformizGrantedAuthority(String role, String entityId, String email) {
        this.role = role;
        this.entityId = entityId;
        this.email = email;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    public String getRole() {
        return role;
    }

/*
    public String getEntityId() {
        return entityId;
    }
*/
}
