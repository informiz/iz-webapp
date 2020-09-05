package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name="reference")
@Entity
public class Reference extends InformizEntity implements Serializable {

    static final long serialVersionUID = 1L;

    public enum Entailment {
        SUPPORTS("Supports"),
        CONTRADICTS("Contradicts"),
        IRRELEVANT("Irrelevant");

        private final String displayValue;

        private Entailment(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    // The reviewed-entity (always in the local channel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_entity_id")
    @JsonIgnore
    private ChainCodeEntity reviewed;


    // The citation's entity-id on the ledger
    @Column(name = "citation_entity_id")
    @NotBlank
    private String citationId;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private Entailment entailment;

    @Column
    private String comment;

    public Reference() {}

    public Reference(ChainCodeEntity reviewed, String citationId, Entailment entailment, String comment) {
        this.reviewed = reviewed;
        this.citationId = citationId;
        this.entailment = entailment;
        this.comment = comment;
    }

    public ChainCodeEntity getReviewed() {
        return reviewed;
    }

    public void setReviewed(ChainCodeEntity reviewed) {
        this.reviewed = reviewed;
    }

    public String getCitationId() {
        return citationId;
    }

    public void setCitationId(String citationId) {
        this.citationId = citationId;
    }

    public Entailment getEntailment() {
        return entailment;
    }

    public void setEntailment(Entailment entailment) {
        this.entailment = entailment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return String.format("%s-%s-%s", reviewed.getEntityId(), citationId, entailment.toString()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Reference)) return false;
        Reference other = (Reference) obj;
        return (this.reviewed.getEntityId().equals(other.reviewed.getEntityId()) &&
                this.citationId.equals(other.citationId) &&
                this.entailment.toString().equals(other.entailment.toString()));
    }}
