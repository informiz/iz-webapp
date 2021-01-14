package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
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
    private ChainCodeEntity reviewed; // TODO: rename this field/column


    // The entity-id of the claim/citation on the ledger
    @Column(name = "citation_entity_id")
    @NotBlank
    private String referencedId; // TODO: rename this column

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private Entailment entailment;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    //@NotNull
    // The degree to which the reference entails the entity
    private Float degree = 0.9f;

    @Column
    private String comment;

    public static Reference create() { return new Reference(); }

    public Reference() {}

    public Reference(ChainCodeEntity reviewed, Reference other) {
        this.reviewed = reviewed;
        this.referencedId = other.referencedId;
        this.entailment = other.entailment;
        this.degree = other.degree;
        this.comment = other.comment;
    }

    public ChainCodeEntity getReviewed() {
        return reviewed;
    }

    public void setReviewed(ChainCodeEntity reviewed) {
        this.reviewed = reviewed;
    }

    public String getReferencedId() {
        return referencedId;
    }

    public void setReferencedId(String referencedId) {
        this.referencedId = referencedId;
    }

    public Entailment getEntailment() {
        return entailment;
    }

    public void setEntailment(Entailment entailment) {
        this.entailment = entailment;
    }

    public Float getDegree() {
        return degree;
    }

    public void setDegree(Float degree) {
        this.degree = degree;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return String.format("%s-%s-%s", reviewed.getEntityId(), referencedId, creatorId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || ! (obj instanceof Reference)) return false;
        Reference other = (Reference) obj;

        // considered equal if same entity, claim/citation and creator
        return (this.reviewed.getEntityId().equals(other.reviewed.getEntityId()) &&
                this.referencedId.equals(other.referencedId) &&
                // creatorId is only set when persisting the reference to db. TODO: is this an issue?
                ( (this.creatorId == null && other.creatorId == null) ||
                        this.creatorId.equals(other.creatorId)));
    }}
