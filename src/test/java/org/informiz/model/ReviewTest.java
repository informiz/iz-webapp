package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ReviewTest extends IzEntityTestBase<Review> {

    //Valid   (Default, ExistingEntity, DeleteEntity, NewUserReview, ExistingUserReview)
    @Test
    public void whenValidReview_thenDefaultValidatorSucceeds() {
        Review review = ModelTestUtils.getPopulatedReview();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReview_thenExistingEntityFromUIValidatorSucceeds() {
        Review review = new Review();

        review.setId(1L);
        review.setOwnerId("TestOwnerId");

        Set<ConstraintViolation<Review>> violations = validator.validate(review, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReview_thenDeleteEntityValidatorSucceeds() {
        Review review = new Review();

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        Set<ConstraintViolation<Review>> violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReview_thenNewUserReviewValidatorSucceeds() {
        Review review = new Review();

        review.setRating(0.9f);
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        //review.setComment("Testing Review valid comment");

        Set<ConstraintViolation<Review>> violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReview_thenExistingUserReviewValidatorSucceeds() {
        Review review = new Review();

        review.setId(1l);
        review.setOwnerId("1l");

        review.setRating(0.9f);
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        review.setComment("Testing Review valid comment");

        Set<ConstraintViolation<Review>> violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(0, violations.size());
    }

    // ID    {!Null}  (Default, ExistingEntity, DeleteEntity, NewUserReview)
    @Test
    public void whenIdIsNull_thenDefaultValidatorViolation() {
        Review review = getValidEntity();

        review.setId(null);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenIdIsNull_thenExistingEntityValidatorViolation() {
        Review review = new Review();

        review.setOwnerId("TestOwnerId");

        review.setId(null);

        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenIdIsNull_thenDeleteEntityValidatorViolation() {
        Review review = new Review();

        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        review.setOwnerId("TestOwnerId");

        review.setId(null);

        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenIdIsNull_thenNewUserReviewValidatorViolation() {
        Review review = new Review();

        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setId(null);

        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }


    // ID   {Positive}  (Default, ExistingEntity, DeleteEntity)

    @Test
    public void whenIdIsNegative_thenDefaultValidatorViolation() {
        Review review = getValidEntity();

        review.setId(-1L);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenIdIsNegative_thenExistingEntityValidatorViolation() {
        Review review = new Review();

        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setId(-1L);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenIdIsNegative_thenDeleteEntityValidatorViolation() {
        Review review = new Review();

        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setId(-1L);
        Set<ConstraintViolation<Review>>
                violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //Rating {!Null} (Default, NewUserReview, ExistingUsrReview)
    @Test
    public void  whenRatingIsNull_thenDefaultValidatorViolation(){
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setRating(null);

        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsNull_thenNewUserReviewValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setRating(null);

        violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsNull_thenExistingUserReviewValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1l);
        review.setOwnerId("1l");

        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        //review.setComment("Testing Review valid comment");

        review.setRating(null);

        violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }

    //Rating {Less_Than_MIN} (Default, NewUserReview, ExistingUsrReview)
    @Test
    public void  whenRatingIsLessThanMinimum_thenDefaultValidatorViolation(){
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setRating(-0.69f);

        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsLessThanMinimum_thenNewUserReviewValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setRating(-0.69f);

        violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsLessThanMinimum_thenExistingUserValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1l);
        review.setOwnerId("1l");

        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        //review.setComment("Testing Review valid comment");

        review.setRating(-0.69f);

        violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }
    //Rating {Greater_Than_MAX} (Default, NewUserReview, ExistingUsrReview)
    @Test
    public void  whenRatingIsGreaterThanMax_thenDefaultValidatorViolation(){
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setRating(1.69f);

        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsGreaterThanMax_thenNewUserReviewValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setRating(1.69f);

        violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void  whenRatingIsGreaterThanMax_thenExistingUserValidatorViolation(){
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1l);
        review.setOwnerId("1l");

        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");
        //review.setComment("Testing Review valid comment");

        review.setRating(1.69f);

        violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }

    //ReviewedEntityId {!Blank} (Default, NewUserReview, ExistingUserReview, DeleteEntity)
    @Test
    public void whenReviewedEntityIdIsBlank_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setReviewedEntityId("");

        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdIsBlank_thenNewUserReviewValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setRating(0.69f);

        review.setReviewedEntityId("");

        violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdIsBlank_thenExistingUserReviewValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setRating(0.69f);

        review.setReviewedEntityId("");

        violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdIsBlank_thenDeleteEntityValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");

        review.setReviewedEntityId("");

        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //ReviewedEntityId {>255} (Default, NewUserReview, ExistingUserReview, DeleteEntity)
    @Test
    public void whenReviewedEntityIdExceeds_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setReviewedEntityId(RandomStringUtils.random(256));
        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdExceeds_thenNewUserReviewValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setRating(0.69f);

        review.setReviewedEntityId(RandomStringUtils.random(256));

        violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdExceeds_thenExistingUserReviewValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");
        review.setRating(0.69f);

        review.setReviewedEntityId(RandomStringUtils.random(256));

        violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewedEntityIdExceeds_thenDeleteEntityValidatorViolation() {
        Review review = new Review();
        Set<ConstraintViolation<Review>> violations;

        review.setId(1L);
        review.setOwnerId("TestOwnerId");

        review.setReviewedEntityId(RandomStringUtils.random(256));

        violations = validator.validate(review, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //Comment Exceeds {Default, NewUserReview, ExistingUserReview}
    @Test
    public void whenReviewCommentExceeds_thenDefaultValidatorViolation() {
        Review review = ModelTestUtils.getPopulatedReview();
        Set<ConstraintViolation<Review>> violations;

        review.setComment(RandomStringUtils.random(256));
        violations = validator.validate(review);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenReviewCommentExceeds_thenNewUserReviewValidatorViolation() {
        Review review = new Review();

        review.setRating(0.9f);
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setComment(RandomStringUtils.random(256));

        Set<ConstraintViolation<Review>> violations = validator.validate(review, Review.NewUserReview.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenReviewCommentExceeds_thenExistingUserReviewValidatorViolation() {
        Review review = new Review();

        review.setId(1l);
        review.setOwnerId("1l");

        review.setRating(0.9f);
        review.setReviewedEntityId("SomeEntityIdOfReasonableLength");

        review.setComment(RandomStringUtils.random(256));

        Set<ConstraintViolation<Review>> violations = validator.validate(review, Review.ExistingUserReview.class);
        assertEquals(1, violations.size());
    }
    @NotNull
    @Override
    protected Review getValidEntity() {
        return ModelTestUtils.getPopulatedReview();
    }
}