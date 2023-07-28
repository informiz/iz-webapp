package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import java.util.*;
import java.util.function.Consumer;

import static org.informiz.model.Score.CONFIDENCE_BOOST;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonView(Utils.Views.EntityDefaultView.class)
//@MappedSuperclass
public abstract class ChainCodeEntity extends InformizEntity {

    static final long serialVersionUID = 3L ;

    protected static ObjectMapper mapper = new ObjectMapper();


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @NotNull(message = "Please provide an ID", groups = { DeleteEntity.class, PostInsertDefault.class})
    @Positive(groups = { DeleteEntity.class, Default.class })
    public Long id;

    public Long getLocalId() {
        return id;
    }

    public void setLocalId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public ChainCodeEntity setId(Long id) {
        this.id = id;
        return this;
    }

    // Unique entity identifier
    @Column(name = "entity_id", unique = true)
    @Access(AccessType.PROPERTY)
    @NotNull(message = "Please provide an entity-ID", groups = { DeleteEntity.class, Default.class })
    @Size(message = "Entity-ID is expected to be 25-255 characters long", min = 25, max = 255, groups = { DeleteEntity.class, Default.class })
    protected String entityId;

    @NotNull(message = "Locale is mandatory")
    private Locale locale = Locale.ENGLISH; // Default to English

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval=true)
    @JoinColumn(name = "reviewed_entity_id", referencedColumnName = "entity_id")
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
            score = new Score(sumRatings.floatValue()/numReviews, Math.min(0.99f, CONFIDENCE_BOOST * numReviews));
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

    // TODO: can't target sub-classes with entity_id as ref-column, fixed in Hibernate 6.3
    // TODO: see https://hibernate.atlassian.net/browse/HHH-16501, move collections to subclasses when 6.3 available

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval=true,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "fact_checked_entity_id", referencedColumnName = "entity_id")
    @JsonView(Utils.Views.EntityData.class)
    protected Set<Reference> references = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval=true,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_sourced_entity_id", referencedColumnName = "entity_id")
    @JsonView(Utils.Views.EntityData.class)
    protected Set<SourceRef> sources = new HashSet<>();


    // TODO: Currently only Hypothesis and Citation have sources, need to sort out class-hierarchy
    public Set<SourceRef> getSources() {
        return sources;
    }

    public void setSources(Set<SourceRef> sources) {
        this.sources = sources;
    }

    public boolean removeSource(@NotNull Long srcRefId, @NotNull String owner) {
        List<SourceRef> snapshot = new ArrayList(sources);
        SourceRef ref = snapshot.stream().filter(reference ->
                srcRefId.equals(reference.getId()) && owner.equals(reference.getOwnerId()))
                .findFirst().orElse(null);

        if (ref != null)
            return sources.remove(ref);
        return false;
    }

    public boolean addSource(@NotNull SourceRef srcRef) {
        boolean bool;
        // TODO: Source references are considered equal if they have the same well-known source and link.
        //       This will do nothing if user(s) add the same source multiple times.
        synchronized (sources) {
            bool = getSources().add(srcRef);
        }
        return bool;
    }




}
