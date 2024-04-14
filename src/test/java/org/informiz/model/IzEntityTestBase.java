package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Set;

import static org.informiz.model.InformizEntity.DeleteEntity;
import static org.informiz.model.InformizEntity.ExistingEntityFromUI;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(IzEntityTestBase.Config.class)
public abstract class IzEntityTestBase<T extends InformizEntity<InformizEntity>> {
    protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static class Config {}

    //Valid
    @Test
    public void whenValidEntity_thenDefaultValidatorSucceeds() {
        T entity = getValidEntity();
        Set<ConstraintViolation<T>>

        violations = validator.validate(entity);
        assertEquals(0, violations.size());
    }

    //************* ID >>  !Null, Positive
    //!Null
    @Test
    public void whenIDIsNull_thenPostInsertDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity, ExistingEntityFromUI.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenIDIsNull_thenDeleteEntityValidatorViolation() {
        T entity = getValidEntity();

        entity.setId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity, DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //************* creatorId test
    @Test
    public void whenCreatorIDisBlank_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatorId("");

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatorIdExceeds_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatorId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    //*******    OwnerID test
    @Test
    public void whenOwnerIDisNull_thenDeleteEntityValidatorViolation() {
        T entity = getValidEntity();

        entity.setOwnerId(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity, DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenOwnerIdExceeds_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setOwnerId(RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenCreatedTsIsNull_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    @Disabled("Not sure how to test Ts")
    public void whenCreatedTsInvalid_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setCreatedTs(123456L);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenUpdtedTsIsNull_thenDefaultValidatorViolation() {
        T entity = getValidEntity();

        entity.setUpdatedTs(null);

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        assertEquals(1, violations.size());
    }

    @NotNull
    protected abstract T getValidEntity();
}
