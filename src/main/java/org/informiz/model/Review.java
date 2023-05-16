package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Formula;

import java.io.Serializable;

@Table(name="review")
@Entity
public final class Review extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;
    public static final String QUERY = "(IF reviewed_entity_id like FACT_CHECKER_% " +
            "SELECT * FROM fact_checker fc where fc.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like CITATION_% " +
            "SELECT * FROM citation c where c.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like SOURCE_% " +
            "SELECT * FROM source s where s.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like CLAIM_% " +
            "SELECT * FROM claim c where c.entity_id = reviewed_entity_id " +
            "ELSE " +
            "SELECT * FROM informi i where i.entity_id = reviewed_entity_id)";

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @NotNull
    private Float rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @Formula(value=QUERY)
    @JoinColumn(name = "reviewed_entity_id", referencedColumnName = "entity_id")
    @JsonIgnore
    private ChainCodeEntity reviewed;

    @Column
    private String comment;

    public static Review create() { return new Review(); }

    public Review() {}

    public Review(ChainCodeEntity reviewed, Float rating, String comment) {
        this.reviewed = reviewed;
        this.rating = rating;
        this.comment = comment;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public ChainCodeEntity getReviewed() {
        return reviewed;
    }

    public void setReviewed(ChainCodeEntity reviewed) {
        this.reviewed = reviewed;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return String.format("%s-%s", creatorId, reviewed.getEntityId()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Review)) return false;
        Review other = (Review) obj;
        return (this.creatorId.equalsIgnoreCase(other.creatorId) &&
                this.reviewed.getEntityId().equals(other.reviewed.getEntityId()));
    }
}
