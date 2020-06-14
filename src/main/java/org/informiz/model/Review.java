package org.informiz.model;

import javax.persistence.*;

@Table(name="review")
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    // The fact-checker's id on the ledger (may be from a different channel, so not necessarily in the local db)
    private String checker;

    private Float rating;

    @ManyToOne
    @JoinColumn(name = "entity_id")
    private ChainCodeEntity reviewed;

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
}
