package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.URL;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

@Table(name="source_ref")
@Entity
public final class SourceRef extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    public static final String SRC_QUERY = "(SELECT * FROM source s where s.entity_id = src_entity_id)";

    // TODO: replace fk_sourced_entity_id (local id) with actual sourced_entity_id
    public static final String QUERY = "(IF (select count(c) = 1 from claim c where id = fk_sourced_entity_id) " +
            "SELECT * FROM claim c where c.id = fk_sourced_entity_id " +
            "ELSE " +
            "SELECT * FROM citation c where c.id = fk_sourced_entity_id)";
/*
    public static final String QUERY = "(IF sourced_entity_id like CLAIM_% " +
            "SELECT * FROM claim c where c.entity_id = sourced_entity_id " +
            "ELSE " +
            "SELECT * FROM citation c where c.entity_id = sourced_entity_id)";
*/

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Mapping both srcEntityId and source to same column - value is not insertable/updatable
    @Column(name = "src_entity_id", insertable=false, updatable=false)
    private String srcEntityId;


    // TODO: can't target SourceBase with entity_id as ref-column, fixed in Hibernate 6.3
    // TODO: see https://hibernate.atlassian.net/browse/HHH-16501
    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    @JoinColumn(name = "src_entity_id", referencedColumnName = "entity_id")
    @Formula(value=SRC_QUERY)
    @JsonIgnore
    private ChainCodeEntity source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_sourced_entity_id") // TODO: need to change in DB
    @Formula(value=QUERY)
    @JsonIgnore
    private ChainCodeEntity sourced;

    @URL
    private String link;

    private String description;


    public SourceRef() {}


    public SourceRef(SourceBase src, ChainCodeEntity sourced, String link, String description) {
        if (src == null && StringUtils.isBlank(link)) {
            throw new IllegalArgumentException("Please provide either a link, a source or both");
        }
        this.sourced = sourced;
        this.source = src;
        if (src != null) srcEntityId = src.getEntityId();
        this.link = link;
        this.description = description;
    }

    @Nullable
    public ChainCodeEntity getSource() {
        return source;
    }

    public void setSource(@Nullable SourceBase source) {
        this.source = source;
    }

    public String getSrcEntityId() {
        return (source == null) ? srcEntityId : source.entityId;
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
        return String.format("%s-%s-%s", getSrcEntityId(), sourced.getEntityId(), link).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( ! (obj instanceof SourceRef other)) return false;

        boolean sameLink = Objects.equals(link, other.link);
        boolean sameSrc = getSrcEntityId() == null ?
                other.getSrcEntityId() == null :
                getSrcEntityId().equals(other.getSrcEntityId());
        return (sourced.equals(other.sourced) && sameLink && sameSrc);
    }
}
