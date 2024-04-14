package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceTest extends IzEntityTestBase<Reference> {

    //  Valid (Default, NewUserReference, ExistingUserReference)
    @Test
    public void whenValidReference_thenDefaultValidatorSucceeds() {
        Reference reference = getValidEntity();

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReference_thenNewUserReferenceValidatorSucceeds() {
        Reference reference = new Reference();

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReference_thenExistingUserReferenceValidatorSucceeds() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidReference_thenDeleteEntityValidatorSucceeds() {
        Reference reference = new Reference();

        reference.setId(1l);
        reference.setOwnerId("1l");
        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(0, violations.size());
    }

    //FactCheckedEntityId   {!Blank} (Default, NewUserRef, ExistingUserRef, DeleteNTT)
    @Test
    public void whenFactCheckedEntityIdIsBlank_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setFactCheckedEntityId("");
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityIsBlank_thenNewUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setFactCheckedEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityIdIsBlank_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setFactCheckedEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityIdIsBlank_thenDeleteEntityValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setRefEntityId("12345l");
        //reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setFactCheckedEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }


    //  FactCheckedEntityId     {<255>} (Default, NewUserRef, ExistingUserRef, DeleteNTT)
    @Test
    public void whenFactCheckedEntityIdExceeds_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setFactCheckedEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityExceeds_thenNewUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setFactCheckedEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityExceeds_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setFactCheckedEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenFactCheckedEntityIdExceeds_thenDeleteEntityValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setRefEntityId("12345l");

        reference.setFactCheckedEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    // RefEntityId {!Blank} (Default, NewUserRef, ExistingUserRef, DeleteNTT)
    @Test
    public void whenRefEntityIdIsBlank_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setRefEntityId("");

        Set<ConstraintViolation<Reference>>
        violations = validator.validate(reference);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");

    }

    @Test
    public void whenRefEntityIdIsBlank_thenNewUserReferenceValidatorViolation() {
        Reference reference =new Reference();

        reference.setFactCheckedEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setRefEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }
    @Test
    public void whenRefEntityIdIsBlank_thenExistingUserReferenceValidatorViolation() {
        Reference reference =new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setRefEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }
    @Test
    public void whenRefEntityIdIsBlank_thenDeleteEntityValidatorViolation() {
        Reference reference =new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");

        reference.setRefEntityId("");

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }

    // RefEntityId {<255>} (Default, NewUserRef, ExistingUserRef, DeleteNTT)
    @Test
    public void whenRefEntityIdExceeds_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setRefEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");

    }
    @Test
    public void whenRefEntityIdExceeds_thenNewUserReferenceValidatorViolation() {
        Reference reference =new Reference();

        reference.setFactCheckedEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setRefEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }
    @Test
    public void whenRefEntityIdExceeds_thenExistingUserReferenceValidatorViolation() {
        Reference reference =new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setRefEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }
    @Test
    public void whenRefEntityIdExceeds_thenDeleteEntityValidatorViolation() {
        Reference reference =new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");

        reference.setRefEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size(), "Expected Blank RefID Violation");
    }


    //Entailment   {!Null} (Default, NewUserRef, ExistingUserRef)

    @Test
    public void whenEntailmentIsNull_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setEntailment(null);

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenEntailmentIsNull_thenNewUserReferenceValidatorSucceeds() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");

        reference.setEntailment(null);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenEntailmentIsNull_thenExistingUserReferenceValidatorSucceeds() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");

        reference.setEntailment(null);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size());
    }

    //Degree {!Null} (Default, NewUserRef, ExistingUserRef)
    @Test
    public void whenDegreeIsNull_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();
        Set<ConstraintViolation<Reference>> violations;

        reference.setDegree(null);

        violations = validator.validate(reference);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }
    @Test
    public void whenDegreeIsNull_thenNewUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(null);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    @Test
    public void whenDegreeIsNull_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(null);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    //Degree {Less_Than_Min} (Default, NewUserRef, ExistingUserRef)
    @Test
    public void whenDegreeIsLessThanMinimum_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();
        Set<ConstraintViolation<Reference>> violations;

        reference.setDegree(-0.69f);

        violations = validator.validate(reference);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }
    @Test
    public void whenDegreeIsLessThanMinimum_thenNewUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(-0.69f);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    @Test
    public void whenDegreeIsLessThanMinimum_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(-0.69f);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    //Degree {More_Than_Max} (Default, NewUserRef, ExistingUserRef)
    @Test
    public void whenDegreeIsMoreThanMaximum_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();
        Set<ConstraintViolation<Reference>> violations;

        reference.setDegree(1.69f);

        violations = validator.validate(reference);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }
    @Test
    public void whenDegreeIsMoreThanMaximum_thenNewUserRefere2nceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(1.69f);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    @Test
    public void whenDegreeIsMoreThanMaximum_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setDegree(1.69f);

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }



    //Comment  {<255}  (Default, NewUserRef, ExistingUserRef)
    @Test
    public void whenCommentExceeds_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setComment(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenCommentExceeds_thenNewUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setComment(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.NewUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }
    @Test
    public void whenCommentExceeds_thenExistingUserReferenceValidatorViolation() {
        Reference reference = new Reference();

        reference.setId(1L);
        reference.setOwnerId("TestOwnerId");

        reference.setFactCheckedEntityId("12345l");
        reference.setRefEntityId("12345l");
        reference.setEntailment(Reference.Entailment.SUPPORTS);

        reference.setComment(RandomStringUtils.random(256));

        Set<ConstraintViolation<Reference>> violations = validator.validate(reference, Reference.ExistingUserReference.class);
        assertEquals(1, violations.size(), "Expected Null Degree Violation");
    }

    //********************
    /*// ID    >>    !Null, Pos{DeleteEntity}
    @Test
    public void whenIDisNull_thenDeleteEntityValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setId(null);
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //Positive
    @Test
    public void whenIDisNegative_thenDeleteEntityValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setId(-1L);
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenIdIsNegative_thenDefaultValidatorViolation() {
        Reference reference = getValidEntity();

        reference.setId(-1L);
        Set<ConstraintViolation<Reference>>
                violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }*/

    //*******************
    @NotNull
    protected Reference getValidEntity() {
        InformiBase informi = getPopulatedInformi(1l);
        Reference reference = ModelTestUtils.getPopulatedReference(informi, "TestReference", 1l);
        return reference;
    }
}
