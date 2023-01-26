package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name="source_ref")
@Entity
public class SourceRef extends InformizEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Column(name = "src_entity_id")
    private String srcEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_sourced_entity_id")
    @JsonIgnore
    private ChainCodeEntity sourced;

    @URL
    private String link;

    private String description;


    public SourceRef() {}

    public SourceRef(String srcEntityId, ChainCodeEntity sourced, String link, String description) {
        if (StringUtils.isBlank(srcEntityId) && StringUtils.isBlank(link)) {
            throw new IllegalArgumentException("Please provide either a link, a source or both");
        }
        this.sourced = sourced;
        this.srcEntityId = srcEntityId;
        this.link = link;
        this.description = description;
    }

    public String getSrcEntityId() {
        return srcEntityId;
    }

    public void setSrcEntityId(String srcEntityId) {
        this.srcEntityId = srcEntityId;
    }

    public ChainCodeEntity getSourced() {
        return sourced;
    }

    public void setSourced(ChainCodeEntity sourced) {
        this.sourced = sourced;
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
        return String.format("%s-%s-%s", srcEntityId, sourced.getEntityId(), link).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof SourceRef)) return false;
        SourceRef other = (SourceRef) obj;

        boolean sameLink = link == null ? other.link == null : link.equals(other.link);
        boolean sameSrc = srcEntityId == null ? other.srcEntityId == null : srcEntityId.equals(other.srcEntityId);
        return (sourced.equals(other.sourced) && sameLink && sameSrc);
    }
}
