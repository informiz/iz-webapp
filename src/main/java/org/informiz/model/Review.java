package org.informiz.model;

import javax.persistence.*;
import java.io.Serializable;

@Table(name="review")
@Entity
public class Review implements Serializable {

    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    // The fact-checker's id on the ledger (may be from a different channel, so not necessarily in the local db)
    private String checker;

    private Float rating;

/*
    @ManyToOne
    @JoinColumn(name = "entity_id")
*/
    private String reviewed;

    public Review() {}

    public Review(String fcid, String entityId, Float rating) {
        this.checker = fcid;
        this.reviewed = entityId;
        this.rating = rating;
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
}
