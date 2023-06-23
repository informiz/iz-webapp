package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForLong;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

@Table(name="review")
@Entity
public final class Review extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    /**
     * Validation group for incoming review from UI (most fields will not be initialized)
     */
    public interface UserReview {}


    public static final String QUERY = "(IF reviewed_entity_id like FACT_CHECKER_% " +
            "SELECT * FROM fact_checker fc where fc.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like CITATION_% " +
            "SELECT * FROM citation c where c.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like SOURCE_% " +
            "SELECT * FROM source s where s.entity_id = reviewed_entity_id " +
            "ELSE IF reviewed_entity_id like CLAIM_% " +
            "SELECT * FROM claim c where c.entity_id = reviewed_entity_id " +
            "ELSE " +
            "SELECT * FROM informi i where i.entity_id = reviewed_entity_id)";

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    protected Long id;

    public Long getId() {
        return (id == null) ? 0 : id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @DecimalMin(value = "0.0", groups = { UserReview.class, Default.class })
    @DecimalMax(value = "1.0", groups = { UserReview.class, Default.class })
    @NotNull(groups = { UserReview.class, Default.class })
    @Column(name = "rating", nullable = false)
    private Float rating;


    // Mapping both reviewedEntityId and reviewed to same column - value is not insertable/updatable
    @Column(name = "reviewed_entity_id", nullable = false, insertable=false, updatable=false)
    @NotBlank
    @Size(max = 255)
    private String reviewedEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Formula(value=QUERY)
    @JoinColumn(name = "reviewed_entity_id", referencedColumnName = "entity_id")
    @JsonIgnore
    private ChainCodeEntity reviewed;

    @Column(name = "comment")
    @Size(max = 255)
    private String comment;

    public static Review create() { return new Review(); }

    public Review() {}

    public Review(@NotNull ChainCodeEntity reviewed, Float rating, String comment) {
        this.reviewed = reviewed;
        this.reviewedEntityId = reviewed.getEntityId();
        this.rating = rating;
        this.comment = comment;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public ChainCodeEntity getReviewed() {
        return reviewed;
    }

    public void setReviewed(ChainCodeEntity reviewed) {
        this.reviewed = reviewed;
    }

    public String getReviewedEntityId() {
        return reviewedEntityId;
    }

    public void setReviewedEntityId(String reviewedEntityId) {
        this.reviewedEntityId = reviewedEntityId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return String.format("%s-%s", creatorId, reviewedEntityId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Review)) return false;
        Review other = (Review) obj;
        return (this.creatorId.equalsIgnoreCase(other.creatorId) &&
                this.reviewedEntityId.equals(other.reviewedEntityId));
    }

    // Custom validation for review-deletion requests coming from the UI - annotation and validator definitions
    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ReviewDeletionValidator.class)
    public @interface ReviewDeletion {
        String message() default "Review deletion requires owner-id and review-id";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    // A validator for verifying review-deletion required fields
    public static class ReviewDeletionValidator implements ConstraintValidator<ReviewDeletion, Review> {
        private final static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        private final static ConstraintValidator isLong = new PositiveValidatorForLong();
        private final static ConstraintValidator isNotNull = new NotNullValidator();
        @Override
        public void initialize(ReviewDeletion constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(Review review, ConstraintValidatorContext context) {
            Set<ConstraintViolation<Review>> violations = validator.validateProperty(review, "ownerId");
            violations.addAll(validator.validateProperty(review, "id"));
            return violations.isEmpty() &&
                    isNotNull.isValid(review.getId(), context) &&
                    isLong.isValid(review.getId(), context);
        }
    }
}
