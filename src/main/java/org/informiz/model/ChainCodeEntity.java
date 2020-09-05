package org.informiz.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ChainCodeEntity extends InformizEntity {

    static final long serialVersionUID = 1L;

    // TODO: better way to get channel name?
    static final String channelName = System.getProperty("iz.channel.name");

    public enum EntityType {
        FACT_CHECKER("Fact Checker"),
        SOURCE("Source"),
        CLAIM("Claim"),
        CITATION("Citation"),
        INFORMI("Informi");

        private final String displayValue;

        private EntityType(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

    }

    protected static ObjectMapper mapper = new ObjectMapper();


    // The entity's id on the ledger
    @Column(name = "entity_id", unique = true)
    protected String entityId;

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


    // TODO: ************************ REMOVE THIS ONCE ENTITY ID IS PROVIDED BY CHAINCODE ************************

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        entityId = createEntityId();
    }

    protected String createEntityId() {
        EntityType entityType;

        if (this instanceof FactCheckerBase) {
            entityType = EntityType.FACT_CHECKER;
        } else if (this instanceof SourceBase) {
            entityType = EntityType.SOURCE;
        } else if (this instanceof HypothesisBase) {
            entityType = EntityType.CLAIM;
        } else if (this instanceof CitationBase) {
            entityType = EntityType.CITATION;
        } else if (this instanceof InformiBase) {
            entityType = EntityType.INFORMI;
        } else {
            throw new IllegalStateException("Unexpected entity type: " + this.toString());
        }

        // TODO: check uniqueness
        return String.format("%s_%s_%s",
                entityType, channelName, UUID.randomUUID().toString().substring(0, 16));
    }
    // TODO: ************************ REMOVE THIS ONCE ENTITY ID IS PROVIDED BY CHAINCODE ************************


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
        reviews.add(review);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
    }

    public Review getCheckerReview(String fcid) {
        // TODO: more efficient way?
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
