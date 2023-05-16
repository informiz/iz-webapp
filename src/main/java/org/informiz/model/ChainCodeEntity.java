package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;
import java.util.function.Consumer;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@MappedSuperclass
public abstract class ChainCodeEntity extends InformizEntity {

    static final long serialVersionUID = 3L ;

    protected static ObjectMapper mapper = new ObjectMapper();


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    public Long id;

    public Long getLocalId() {
        return id;
    }

    public void setLocalId(Long id) {
        this.id = id;
    }

    // The entity's id on the ledger
/*  TODO: deprecate local-id?
    @Id
    @GeneratedValue(generator = "entity-id-generator")
    @GenericGenerator(name = "entity-id-generator",
            strategy = "org.informiz.model.EntityIdGenerator")
*/
    @Column(name = "entity_id", unique = true)
    @Access(AccessType.PROPERTY)
    protected String entityId;

    @NotNull(message = "Locale is mandatory")
    private Locale locale = Locale.ENGLISH; // Default to English

    @OneToMany(mappedBy = "reviewed",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval=true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    protected Set<Review> reviews = new HashSet<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score = new Score();

    protected Consumer<InformizEntity> onCreateConsumer() {
        Consumer<InformizEntity> consumer = super.onCreateConsumer();
        return entity -> {
            consumer.accept(entity);
            this.entityId = Utils.createEntityId(this);
        };
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

    // TODO: check: safe for concurrent reviewing?
    public void addReview(Review review) {
        synchronized (reviews) {
            getReviews().add(review);
        }
        getScore();
    }

    public void removeReview(Review review) {
        synchronized (reviews) {
            getReviews().remove(review);
        }
        getScore();
    }

    public Review getCheckerReview(String fcid) {
        // TODO: more efficient way?
        List<Review> snapshot = new ArrayList(reviews);
        Review byChecker = snapshot.stream().filter(review ->
                fcid.equals(review.getCreatorId())).findFirst().orElse(null);
        return byChecker;
    }

    public Score getScore() { // TODO: Don't save to DB, calc on the fly
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
