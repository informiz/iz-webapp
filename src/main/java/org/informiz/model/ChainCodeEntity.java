package org.informiz.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ChainCodeEntity extends InformizEntity {

    static final long serialVersionUID = 1L;

    protected static ObjectMapper mapper = new ObjectMapper();


    // The entity's id on the ledger
    @Column(name = "entity_id", unique = true)
    protected String entityId;

    @Column(columnDefinition = "boolean default 1")
    protected Boolean active;

    @NotNull(message = "Locale is mandatory")
    private Locale locale = Locale.ENGLISH; // Default to English

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL)
    protected Set<Review> reviews;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score = new Score();


    @PrePersist
    protected void onCreate() {
        super.onCreate();
        entityId = Utils.createEntityId(this);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        synchronized (reviews) {
            reviews.add(review);
        }
    }

    public void removeReview(Review review) {
        synchronized (reviews) {
            reviews.remove(review);
        }
    }

    public Review getCheckerReview(String fcid) {
        // TODO: more efficient way?
        List<Review> snapshot = new ArrayList(reviews);
        Review byChecker = snapshot.stream().filter(review ->
                fcid.equals(review.getChecker())).findFirst().orElse(null);
        return byChecker;
    }

    public Score getScore() {
        List<Review> snapshot = new ArrayList(reviews);
        int numReviews = snapshot.size();
        if (numReviews > 0) {
            Double sumRatings = snapshot.stream().mapToDouble(review -> review.getRating()).sum();
            score = new Score(sumRatings.floatValue()/numReviews, Math.min(0.99f, 0.15f * numReviews));
        }

        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        return String.format("Entity: %s", entityId);
    }

    public static String toEntityString(ChainCodeEntity entity) {
        try {
            return mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize entity", e);
        }
    }

    public void edit(@NotNull ChainCodeEntity other) {
        this.setLocale(other.getLocale());
        // TODO: Replace with score calculation, no direct edit
        this.setScore(other.getScore());
    }

    public static <T extends ChainCodeEntity> T fromEntityString(@NotBlank String jsonStr, Class<T> clazz) {
        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Failed to deserialize entity of type %s", clazz), e);
        }
    }
}
