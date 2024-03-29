package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ReviewTest extends IzEntityTestBase<Review> {

    //Default
    @Test
    public void whenValidReview_thenDefaultValidatorSucceeds() {
        Review review = ModelTestUtils.getPopulatedReview();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertEquals(0, violations.size());
    }
    //Todo Add Default review test for DeleteEntity

    // ID    >>    !Null, Pos{DeleteEntity}
    //!Null
    @Test
    public void whenReviewIdIsNull_thenDeleteEntityValidatorViolation() {
        Review review = getValidEntity();

        review.setId(null);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //Positive {DeleteEntity}
    @Test
    public void whenReviewIdIsNegative_thenDeleteEntityValidatorViolation() {
        Review review = getValidEntity();

        review.setId(-1L);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenPreInsertIDisNegative_thenDefaultValidatorViolation() {
        Review review = getValidEntity();

        review.setId(-1L);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review);
        assertEquals(1, violations.size());
    }

    //ReviewedEntityId {Default}
    @Test
    public void whenInvalidEntityId_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        // allows null
        review.setReviewedEntityId(null);
        violations = validator.validate(review);
        assertEquals(0, violations.size());

        // allows empty
        review.setReviewedEntityId("");
        violations = validator.validate(review);
        assertEquals(0, violations.size());

        // max 255 characters
        review.setReviewedEntityId(RandomStringUtils.random(256));
        violations = validator.validate(review);
        assertEquals(1, violations.size());

        // valid - no more errors
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        violations = validator.validate(review);
        assertEquals(0, violations.size());
    }

    //ReviewedEntityId {DeleteEntity}
    @Test
    public void whenInvalidEntityId_thenDeleteEntityValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        // not null
        review.setReviewedEntityId(null);
        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

        // not empty
        review.setReviewedEntityId("");
        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

        // max 255 characters
        review.setReviewedEntityId(RandomStringUtils.random(256));
        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

        // valid - no more errors
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(0, violations.size());
    }

    //ReviewedEntityId {UserReview}
    @Test
    public void whenInvalidEntityId_thenUserReviewValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        // not null
        review.setReviewedEntityId(null);
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // not empty
        review.setReviewedEntityId("");
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // max 255 characters
        review.setReviewedEntityId(RandomStringUtils.random(256));
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // valid - no more errors
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(0, violations.size());
    }

    //Comment Exceeds {Default}
    @Test
    public void whenReviewCommentExceeds_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setComment(RandomStringUtils.random(256));
        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }

    //Comment Exceeds {UserReview}
    @Test
    public void whenReviewCommentExceeds_thenUserReviewValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setComment(RandomStringUtils.random(256));
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());
    }



    //ReviewRating

    @Test
    public void whenInvalidRating_thenUserReviewValidatorViolation() {
        Review review = new Review();
        review.setReviewedEntityId("test");
        Set<ConstraintViolation<Review>> violations;

        // null rating
        review.setRating(null);
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // negative value
        review.setRating(-0.1f);
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // value > 1.0
        review.setRating(1.01f);
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(1, violations.size());

        // valid, no more errors
        review.setRating(1.0f);
        violations = validator.validate(review, Review.UserReview.class);
        assertEquals(0, violations.size());

    }

    @NotNull
    @Override
    protected Review getValidEntity() {
        return ModelTestUtils.getPopulatedReview();
    }
}