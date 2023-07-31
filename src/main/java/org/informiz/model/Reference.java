package org.informiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

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

    /**
     * Validation group for incoming reference from UI (most fields will not be initialized)
     */
    public interface UserReference {}

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
    @NotNull(message = "Please provide an ID", groups = { DeleteEntity.class, PostInsertDefault.class})
    @Positive(groups = { DeleteEntity.class, Default.class })
    protected Long id;

    public Long getId() {
        return id;
    }

    public Reference setId(Long id) {
        this.id = id;
        return this;
    }


    @Column(name = "fact_checked_entity_id")
    @NotBlank()
    @Size(max = 255)
    private String factCheckedEntityId;

    // The entity-id of the claim/citation on the ledger. TODO: validation??
    @Column(name = "ref_entity_id")
    @NotBlank(groups = { UserReference.class, Default.class })
    @Size(max = 255, groups = { UserReference.class, Default.class })
    private String refEntityId; // either hypothesis or citation

    @Enumerated(EnumType.ORDINAL)
    @NotNull(groups = { UserReference.class, Default.class })
    private Entailment entailment;

    @DecimalMin(value = "0.0", groups = { UserReference.class, Default.class })
    @DecimalMax(value = "1.0", groups = { UserReference.class, Default.class })
    @NotNull(groups = { UserReference.class, Default.class })
    // The degree to which the reference entails the entity
    private Float degree = 0.9f;

    @Column
    @Size(max = 255, groups = { UserReference.class, Default.class })
    private String comment;

    public static Reference create() { return new Reference(); }

    public Reference() {}

    public Reference(@NotNull ChainCodeEntity factChecked, @NotNull Reference other) {
        this.factCheckedEntityId = factChecked.getEntityId();
        this.refEntityId = other.refEntityId;
        this.entailment = other.entailment;
        this.degree = other.degree;
        this.comment = other.comment;
    }

    public Reference(@NotNull Reference other) {
        this.factCheckedEntityId = other.getFactCheckedEntityId();
        this.refEntityId = other.getRefEntityId();
        this.entailment = other.getEntailment();
        this.degree = other.getDegree();
        this.comment = other.getComment();
    }

    public String getFactCheckedEntityId() {
        return factCheckedEntityId;
    }

    public void setFactCheckedEntityId(String factCheckedEntityId) {
        this.factCheckedEntityId = factCheckedEntityId;
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
        return String.format("%s-%s-%s", factCheckedEntityId, refEntityId, creatorId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || ! (obj instanceof Reference)) return false;
        Reference other = (Reference) obj;

        // considered equal if same entity, claim/citation and creator
        return (this.factCheckedEntityId.equals(other.factCheckedEntityId) &&
                this.refEntityId.equals(other.refEntityId) &&
                // creatorId is only set when persisting the reference to db.
                ( (this.creatorId == null && other.creatorId == null) ||
                        this.creatorId.equals(other.creatorId)));
    }

    public static Entailment[] entailmentTypes() {
        return Entailment.values();
    }

}
