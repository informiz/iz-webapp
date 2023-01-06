package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name="review")
@Entity
public class Review extends InformizEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // TODO: Remove db constraints on 'checker' column

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @NotNull
    private Float rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_entity_id")
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
