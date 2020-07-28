package org.informiz.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ChainCodeEntity extends InformizEntity {

    static final long serialVersionUID = 1L;

    protected static ObjectMapper mapper = new ObjectMapper();

    // The entity's id on the ledger
    @Column(name = "entity_id", unique = true)
    protected String entityId;

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL)
    //@JoinColumns({ @JoinColumn(name = "FK_entity_id", referencedColumnName = "entity_id") })
    protected Set<Review> reviews; // = new HashSet<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score = new Score();


    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }


    // TODO: ************************ REMOVE THIS ONCE ENTITY ID IS AVAILABLE ************************

    static final Random rand = new Random();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        entityId = String.format("%d-%d", System.currentTimeMillis(), rand.nextInt());
    }
    // TODO: ************************ REMOVE THIS ONCE ENTITY ID IS AVAILABLE ************************

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
    }

    public Review getCheckerReview(String fcid) {
        Review byChecker = reviews.stream().filter(review ->
                fcid.equals(review.getChecker())).findFirst().orElse(null);
        return byChecker;
    }


    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
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
        return entityId;
    }

    public static String toEntityString(ChainCodeEntity entity) {
        try {
            return mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize entity", e);
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
