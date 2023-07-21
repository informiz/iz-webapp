package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Month;
import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class IzEntityTestBase<T extends InformizEntity> {
    protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    protected TestEntityManager entityManager;

    //************* creatorId test
    @Test
    public void whenCreatorIDisNull_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setCreatorId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatorIdExeeds_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setCreatorId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    //*******    OwnerID test
    @Test
    public void whenOwnerIDisNull_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setOwnerId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(reference, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenOwnerIdExceeds_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setOwnerId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatedTsIsNull_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setCreatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    @Test
    @Disabled("Not sure how to test Ts")
    public void whenCreatedTsInvalid_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setCreatedTs(123456L);

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenUpdtedTsIsNull_thenCreatorIdValidatorViolation() {
        T reference = getValidEntity();

        reference.setUpdatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(reference);
        assertEquals(1, violations.size());
    }
    @NotNull
    protected abstract T getValidEntity();
}
