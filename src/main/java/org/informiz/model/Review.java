package org.informiz.model;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Table(name="review")
@Entity
public class Review implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    // The fact-checker's id on the ledger (may be from a different channel, so not necessarily in the local db)
    @Column
    @NotBlank
    private String checker;

    @Column
    @NotBlank
    private String reviewed;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Float rating;

    @Column
    private String comment;

    public Review() {}

    public Review(String fcid, String entityId, Float rating, String comment) {
        this.checker = fcid;
        this.reviewed = entityId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReviewed() {
        return reviewed;
    }

    public void setReviewed(String reviewed) {
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
        return String.format("%s-%s", checker, reviewed).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Review)) return false;
        Review other = (Review) obj;
        return (this.checker.equalsIgnoreCase(other.checker) && this.reviewed.equals(other.reviewed));
    }
}
