package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    protected static ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    protected Long id;

    // The entity's id on the ledger
    @Column(name = "entity_id")
    protected String entityId;

    @OneToMany
    @JoinTable(name = "entity_review",
            joinColumns=
            @JoinColumn(name="reviewed", referencedColumnName="entity_id")
    )
    protected Set<Review> reviews = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Add a review by a fact-checker to this reference-text
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this text before
     */
    public boolean addReview(String fcid, float reliability) {
        // TODO: replace if exists
        return reviews.add(new Review(fcid, getEntityId(), reliability));
    }

    /**
     * Remove a review by a fact-checker from this reference-text
     * @param fcid the fact-checker's id
     * @return the score given by this fact-checker, if one was found
     */
    public void removeReview(String fcid) {
        // TODO: hash-code/equals by entity-id and checker-id
        reviews.remove(fcid);
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }


    // TODO: is this how we want to compare entities?
    @Override
    public int hashCode() {
        return this.entityId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        ChainCodeEntity other = (ChainCodeEntity) obj;

        return this.entityId.equals(other.entityId);
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize fact-checker", e);
        }
    }

    public static <T extends ChainCodeEntity> T fromEntityString(@NotBlank String jsonStr, Class<T> clazz) {
        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Failed to deserialize entity of type %s", clazz), e);
        }
    }

}
