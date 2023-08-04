package org.informiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

import java.io.Serializable;

@Table(name="review")
@Entity
public final class Review extends InformizEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    /**
     * Validation group for incoming review from UI (most fields will not be initialized)
     */
    public interface UserReview {}

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @NotNull(message = "Please provide an ID", groups = { DeleteEntity.class, PostInsertDefault.class })
    @Positive(groups = { DeleteEntity.class, Default.class })
    protected Long id;

    public Long getId() {
        return (id == null) ? 0 : id;
    }

    public Review setId(Long id) {
        this.id = id;
        return this;
    }

    @DecimalMin(value = "0.0", groups = { UserReview.class, Default.class })
    @DecimalMax(value = "1.0", groups = { UserReview.class, Default.class })
    @NotNull(groups = { UserReview.class, Default.class }, message = "Please submit rating between 0.0 and 1.0")
    @Column(name = "rating", nullable = false)
    private Float rating;


    @Column(name = "reviewed_entity_id")
    @NotBlank(groups = { UserReview.class, Default.class, DeleteEntity.class })
    @Size(max = 255, groups = { UserReview.class, Default.class, DeleteEntity.class })
    private String reviewedEntityId;

    @Column(name = "comment")
    @Size(max = 255, groups = { UserReview.class, Default.class }, message = "Comment must be under 255 characters")
    private String comment;

    public static Review create() { return new Review(); }

    public Review() {}

    public Review(@NotNull ChainCodeEntity reviewed, Float rating, String comment) {
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
        return String.format("%s-%s", ownerId, reviewedEntityId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Review)) return false;
        Review other = (Review) obj;
        return (this.ownerId.equalsIgnoreCase(other.ownerId) &&
                this.reviewedEntityId.equals(other.reviewedEntityId));
    }
}
