package org.informiz.auth;

import org.springframework.security.core.GrantedAuthority;

public class InformizGrantedAuthority implements GrantedAuthority {

    // The fact-checker's role
    private String role;

    // The fact-checker's entity-id
    private String entityId;

    public InformizGrantedAuthority(String role, String entityId) {
        this.role = role;
        this.entityId = entityId;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    public String getRole() {
        return role;
    }

    public String getEntityId() {
        return entityId;
    }

}
