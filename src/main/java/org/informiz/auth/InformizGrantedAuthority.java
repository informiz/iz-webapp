package org.informiz.auth;

import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;

public class InformizGrantedAuthority implements GrantedAuthority {

    public static final String ROLE_VIEWER = "ROLE_VIEWER";
    public static final String ROLE_CHECKER = "ROLE_CHECKER";
    public static final String ROLE_MEMBER = "ROLE_MEMBER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // The fact-checker's role
    private String role;

    // The fact-checker's entity-id
    private String entityId;

    public InformizGrantedAuthority(String role, String entityId) {
        this.role = role;
        this.entityId = entityId;
    }

    public static RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(String.format("%s > %s > %s > %s",
                ROLE_ADMIN, ROLE_MEMBER, ROLE_CHECKER, ROLE_VIEWER));
        return roleHierarchy;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getRole() {
        return role;
    }
}
