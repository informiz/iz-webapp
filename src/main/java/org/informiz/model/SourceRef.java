package org.informiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Objects;

@Table(name="source_ref")
@Entity
public final class SourceRef extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    /**
     * Validation group for incoming source-reference from UI (most fields will not be initialized)
     */
    public interface UserSourceReference {}

    public static final String SRC_QUERY = "(SELECT * FROM source s where s.entity_id = src_entity_id)";

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "src_entity_id")
    @Size(max = 255, groups = { UserSourceReference.class, Default.class })
    private String srcEntityId;

    @Column(name = "fk_sourced_entity_id") // TODO: need to change in DB from id to entity-id
    @NotBlank(groups = { UserSourceReference.class, Default.class })
    @Size(max = 255, groups = { UserSourceReference.class, Default.class })
    private String sourcedId;

    @URL(groups = { UserSourceReference.class, Default.class })
    @Size(max = 255, groups = { UserSourceReference.class, Default.class })
    private String link;

    @Size(max = 255, groups = { UserSourceReference.class, Default.class })
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
        return String.format("%s-%s-%s", getSrcEntityId(), srcEntityId, link).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( ! (obj instanceof SourceRef other)) return false;
        return Objects.equals(sourcedId, other.getSourcedId()) && Objects.equals(link, other.getLink()) &&
                Objects.equals(srcEntityId, other.getSrcEntityId()) && Objects.equals(ownerId, other.getOwnerId());
    }
}
