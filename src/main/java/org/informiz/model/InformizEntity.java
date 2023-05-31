package org.informiz.model;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.informiz.auth.InformizGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;

@MappedSuperclass
public abstract class InformizEntity implements Serializable {

    static final long serialVersionUID = 3L;

    @Column(name = "creator_entity_id", nullable = false, updatable = false)
    @NotBlank
    @Size(max = 255)
    protected String creatorId;

    @Column(name = "owner_entity_id", nullable = false)
    @NotBlank
    @Size(max = 255)
    protected String ownerId;

    // Creation time, as UTC timestamp in milliseconds
    @Column(name = "created", nullable = false, updatable = false)
    @NotNull
    protected Long createdTs;

    // Last-updated time, as UTC timestamp in milliseconds
    @Column(name = "last_updated", nullable = false)
    @NotNull
    protected Long updatedTs;

    // Removal time, as UTC timestamp in milliseconds
    @Column(name = "removed")
    protected Long removedTs;

    protected Consumer<InformizEntity> onCreateConsumer() {
            return entity -> {
                entity.createdTs = entity.updatedTs = new Date().getTime();
                try {
                    entity.creatorId = entity.ownerId =
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                            .filter(auth -> (auth instanceof InformizGrantedAuthority))
                            .findFirst()
                            .map(auth -> ((InformizGrantedAuthority) auth).getEntityId()).get();
                } catch (NullPointerException e) {
                    throw new IllegalStateException("Entity creation - no authenticated user found");
                }
        };
    }

    @PrePersist
    protected void onCreate() {
        onCreateConsumer().accept(this);
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
