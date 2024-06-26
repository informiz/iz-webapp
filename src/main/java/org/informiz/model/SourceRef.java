package org.informiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Objects;

@Table(name="source_ref")
@Entity
public final class SourceRef extends InformizEntity<InformizEntity> implements Serializable {

    static final long serialVersionUID = 3L ;

    /**
     * Validation group for incoming source-reference from UI (most fields will not be initialized)
     */
    public interface ExistingUserSourceReference extends ExistingEntityFromUI {}
    public interface NewUserSourceReference{}

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @NotNull (message = "Please provide an ID", groups = { Default.class, ExistingEntityFromUI.class, DeleteEntity.class })
    @Positive(groups = { Default.class, ExistingEntityFromUI.class, DeleteEntity.class })
    protected Long id;

    public Long getId() {
        return id;
    }

    public SourceRef setId(Long id) {
        this.id = id;
        return this;
    }

    @Column(name = "src_entity_id")
    @Size(max = 255, groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class, DeleteEntity.class })
    private String srcEntityId;

    // TODO: ------------------------- need to change in DB from id to entity-id -------------------------
    // TODO: Allow null in db - Hibernate sets to null on remove from parent's sources, then deletes the source-ref
    @Column(name = "sourced_entity_id")
    @NotBlank(groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class, DeleteEntity.class })
    @Size(max = 255, groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class, DeleteEntity.class })
    private String sourcedId;

    @URL(groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class, DeleteEntity.class })
    @Size(max = 255, groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class, DeleteEntity.class })
    private String link;

    @Size(max = 255, groups = { Default.class, NewUserSourceReference.class, ExistingUserSourceReference.class  })
    private String description;


    public SourceRef() {}

    public SourceRef(@NotNull SourceRef other) {
        if (StringUtils.isBlank(other.getSrcEntityId()) && StringUtils.isBlank(other.getLink())) {
            throw new IllegalArgumentException("Please provide either a link, a source or both");
        }
        this.sourcedId = other.getSourcedId();
        this.srcEntityId = other.getSrcEntityId();
        this.link = other.getLink();
        this.description = other.getDescription();
    }

    public String getSourcedId() {
        return sourcedId;
    }

    public void setSourcedId(String sourcedId) {
        this.sourcedId = sourcedId;
    }

    public String getSrcEntityId() {
        return srcEntityId;
    }

    public void setSrcEntityId(String srcEntityId) {
        this.srcEntityId = srcEntityId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return String.format("%s-%s-%s", getOwnerId(), sourcedId, srcEntityId, link).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( ! (obj instanceof SourceRef other)) return false;
        return Objects.equals(sourcedId, other.getSourcedId()) && Objects.equals(link, other.getLink()) &&
                Objects.equals(srcEntityId, other.getSrcEntityId()) && Objects.equals(ownerId, other.getOwnerId());
    }
}
