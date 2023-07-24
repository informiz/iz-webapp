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


    //************* ID Test
    @Test
    public void whenIDIsNull_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    //************* creatorId test
    @Test
    public void whenCreatorIDisNull_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatorId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatorIdExceeds_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatorId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    //*******    OwnerID test
    @Test
    public void whenOwnerIDisNull_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setOwnerId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenOwnerIdExceeds_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setOwnerId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatedTsIsNull_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    @Disabled("Not sure how to test Ts")
    public void whenCreatedTsInvalid_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatedTs(123456L);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenUpdtedTsIsNull_thenCreatorIdValidatorViolation() {
        T entity = getValidEntity();

        entity.setUpdatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }
    @NotNull
    protected abstract T getValidEntity();
}
