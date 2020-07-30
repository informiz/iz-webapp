package org.informiz.model;


import org.informiz.auth.InformizGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class InformizEntity  implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    //@JsonIgnore
    protected Long id;

    @Column(name = "creator_entity_id")
    protected String creatorId;

    // TODO: link to fact-checker entity?
    @Column(name = "owner_entity_id")
    protected String ownerId;

    // Creation time, as UTC timestamp in milliseconds
    @Column(name = "created", nullable = false)
    protected Long createdTs;

    // Last-updated time, as UTC timestamp in milliseconds
    @Column(name = "last_updated", nullable = false)
    protected Long updatedTs;

    // Removal time, as UTC timestamp in milliseconds
    @Column(name = "removed")
    protected Long removedTs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdTs = updatedTs = new Date().getTime();
        try {
            creatorId = ownerId = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .filter(auth -> (auth instanceof InformizGrantedAuthority))
                    .findFirst()
                    .map(auth -> ((InformizGrantedAuthority) auth).getEntityId()).get();
        } catch (NullPointerException e) {
            // TODO: SHOULD NEVER HAPPEN!! Handle this
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTs = new Date().getTime();
    }

    public void remove() {
        removedTs = updatedTs = new Date().getTime();
    }

    public void revive() {
        removedTs = null;
        updatedTs = new Date().getTime();
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Long createdTs) {
        this.createdTs = createdTs;
    }

    public Long getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Long updatedTs) {
        this.updatedTs = updatedTs;
    }

    public Long getRemovedTs() {
        return removedTs;
    }

    public void setRemovedTs(Long removedTs) {
        this.removedTs = removedTs;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

}
