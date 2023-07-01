package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Formula;

import java.io.Serializable;

/**
 * A class for references in support/contradiction of factual claims.
 *
 * Textual Entailment means that the truth of text A follows from text B. The reference class points
 * to those fragments (e.g. a Hypothesis and a supporting Citation), as well as the entailment (in the same example -
 * the entailment is "Supports").
 */
@Table(name="reference")
@Entity
public final class Reference extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    public static final String QUERY = "(IF ref_entity_id like CLAIM_% " +
            "SELECT * FROM claim c where c.entity_id = ref_entity_id " +
            "ELSE " +
            "SELECT * FROM informi i where i.entity_id = ref_entity_id)";

    public enum Entailment {
        SUPPORTS("Supports"), // Positive textual-entailment
        CONTRADICTS("Contradicts"), // Negative textual-entailment
        IRRELEVANT("Irrelevant"); // Non textual-entailment

        private final String displayValue;

        private Entailment(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // The reviewed-entity (always in the local channel)
    @ManyToOne(fetch = FetchType.LAZY)
    @Formula(value=QUERY)
    // TODO: can't target FactCheckedEntity with entity_id as ref-column, fixed in Hibernate 6.3
    // TODO: see https://hibernate.atlassian.net/browse/HHH-16501
    @JoinColumn(name = "fact_checked_entity_id", referencedColumnName = "entity_id")
    @JsonIgnore
    private ChainCodeEntity factChecked;

    // The entity-id of the claim/citation on the ledger. TODO: validation??
    @Column(name = "ref_entity_id")
    @NotBlank
    private String refEntityId; // either hypothesis or citation

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

    public Reference(ChainCodeEntity factChecked, Reference other) {
        this.factChecked = factChecked;
        this.refEntityId = other.refEntityId;
        this.entailment = other.entailment;
        this.degree = other.degree;
        this.comment = other.comment;
    }

    public ChainCodeEntity getFactChecked() {
        return factChecked;
    }

    public void setFactChecked(ChainCodeEntity referenced) {
        this.factChecked = referenced;
    }

    public String getRefEntityId() {
        return refEntityId;
    }

    public void setRefEntityId(String refEntityId) {
        this.refEntityId = refEntityId;
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
        return String.format("%s-%s-%s", factChecked.getEntityId(), refEntityId, creatorId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || ! (obj instanceof Reference)) return false;
        Reference other = (Reference) obj;

        // considered equal if same entity, claim/citation and creator
        return (this.factChecked.getEntityId().equals(other.factChecked.getEntityId()) &&
                this.refEntityId.equals(other.refEntityId) &&
                // creatorId is only set when persisting the reference to db.
                ( (this.creatorId == null && other.creatorId == null) ||
                        this.creatorId.equals(other.creatorId)));
    }}
