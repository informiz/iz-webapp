package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * A data-type managed by the source contract, representing a source for references (e.g the NASA website):
 * - the id of the source
 * - the source's name
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="source")
@Entity
public final class SourceBase extends ChainCodeEntity {

    private String name;

    @URL(message = "Please provide a valid link")
    private String link;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score;

    @ElementCollection
    @CollectionTable(name = "review")
    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "reviewed")
    @MapKeyColumn(name = "checker")
    @Column(name = "rating")
    private Map<String, Float> reviews = new HashMap<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Add a review by a fact-checker to this source
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this source before
     * @see Map#put(Object, Object)
     */
    public Float addReview(String fcid, float reliability) {
        return reviews.put(fcid, reliability);
    }

    /**
     * Remove a review by a fact-checker from this source
     * @param fcid the fact-checker's id
     * @return the score given by this fact-checker, if one was found
     * @see Map#remove(Object)
     */
    public Float removeReview(String fcid) {
        return reviews.remove(fcid);
    }

    public Map<String, Float> getReviews() {
        return reviews;
    }

    public void setReviews(HashMap<String, Float> reviews) {
        this.reviews = reviews;
    }

    public void edit(SourceBase other) {
        this.setName(other.getName());
        this.setLink(other.getLink());
        this.setReviews(new HashMap<>(other.getReviews()));
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }
}
