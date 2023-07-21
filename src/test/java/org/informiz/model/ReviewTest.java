package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class ReviewTest extends IzEntityTestBase<Review> {
    @Test
    public void whenValidReview_thenDefaultValidatorSucceeds() {
        Review review = ModelTestUtils.getPopulatedReview();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertEquals(0, violations.size());
    }

    @Test
    public void whenBadEntityId_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        // not null
        review.setReviewedEntityId(null);
        violations = validator.validate(review);
        assertEquals(1, violations.size());

        // not empty
        review.setReviewedEntityId("");
        violations = validator.validate(review);
        assertEquals(1, violations.size());

        // max 255 characters
        review.setReviewedEntityId(RandomStringUtils.random(256));
        violations = validator.validate(review);
        assertEquals(1, violations.size());

        // valid - no more errors
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        violations = validator.validate(review);
        assertEquals(0, violations.size());
    }

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