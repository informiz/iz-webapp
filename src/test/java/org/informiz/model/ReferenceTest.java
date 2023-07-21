package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class ReferenceTest extends IzEntityTestBase<Reference> {

    @Test
    public void whenValidReference_thenDefaultValidatorSucceeds() {
        Reference reference = getValidEntity();

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference);
        assertEquals(0, violations.size());
    }

    // RefEntityId max 255 characters or null
    @Test
    public void whenLongOrMissingRefID_thenUserReferenceValidatorViolation() {
        Reference reference = getValidEntity();

        //Longer than 255
        reference.setRefEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());

        //null
        reference.setRefEntityId(null);
        violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    //Testing null Entailment

    @Test
    public void whenEntailmentIsNull_thenUserReferenceValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setEntailment(null);
        //reference.setId(null);


        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference , Reference.UserReference.class);
        assertEquals(1, violations.size());
    }

    //UserReference Degree test
    @Test
    public void whenInvalidDegree_thenUserReferenceValidatorViolation() {

        Reference reference = getValidEntity();
//        Review review = new Review();
//        review.setReviewedEntityId("test");
        Set<ConstraintViolation<Reference>> violations;

        // null degree
//        review.setRating(null);
        reference.setDegree(null);
        violations = validator.validate(reference, Reference.UserReference.class);
        assertEquals(1, violations.size());

        // negative value
        reference.setDegree(-0.1f);
        violations = validator.validate(reference, Reference.UserReference.class);
        assertEquals(1, violations.size());


        // value > 1.0
        reference.setDegree(1.01f);
        violations = validator.validate(reference, Reference.UserReference.class);
        assertEquals(1, violations.size());

        // value > 0.0
        reference.setDegree(0.00f);
        violations = validator.validate(reference, Reference.UserReference.class);
        assertEquals(0, violations.size());


        // valid, no more errors
        reference.setDegree(1.0f);
        violations = validator.validate(reference, Reference.UserReference.class);
        assertEquals(0, violations.size());
    }

    //Reference comments
    @Test
    public void whenLongComment_thenUserReferenceValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setComment(RandomStringUtils.random(255));
        //reference.setId(null);


        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference , Reference.UserReference.class);
        assertEquals(0, violations.size());
    }

    //DefaultAssult
    @Test
    public void whenDefaultIsFaulty_thenDefaultValidatorFails() {
        Reference reference = getValidEntity();

        //Entailment_Null
        reference.setEntailment(null);
        Set<ConstraintViolation<Reference>> violations = validator.validate(reference);
        assertEquals(1, violations.size());

        reference.setEntailment(Reference.Entailment.SUPPORTS);
        String validStr = reference.getRefEntityId();

        //RefEntity_Null
        reference.setRefEntityId(null);
        violations = validator.validate(reference);
        assertEquals(1, violations.size());

        reference.setRefEntityId(validStr);


        //Degree>1
        reference.setDegree(1.4f);
        violations = validator.validate(reference);
        assertEquals(1, violations.size());

        reference.setDegree(0.9f);

        //Comment>255
        reference.setComment(RandomStringUtils.random(256));
        violations = validator.validate(reference);
        assertEquals(1, violations.size());

        reference.setComment(RandomStringUtils.random(250));




        reference.setEntailment(null);
        reference.setRefEntityId(RandomStringUtils.random(257));
        reference.setDegree(-1.4f);
        reference.setComment(RandomStringUtils.random(3256));

        violations = validator.validate(reference);
        assertEquals(4, violations.size());


    }


    //********      CreatedTs test

    //********      UpdatedTs


    //*******************
    @NotNull
    protected  Reference getValidEntity() {
        InformiBase informi = getPopulatedInformi(1l);
        Reference reference = ModelTestUtils.getPopulatedReference(informi, "TestReference", 1l);
        return reference;
    }
}
