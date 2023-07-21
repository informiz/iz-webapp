package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

abstract class SourceRefTest <T extends InformizEntity> {
    protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    protected TestEntityManager entityManager;



    @Test
    public void whenOwnerIdExceeds_thenCreatorIdValidatorViolation() {
        T sourceRef = getValidEntity();

        sourceRef.setSourceRef (RandomStringUtils.random(256));

        Set<ConstraintViolation<T>> violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }

    @NotNull
    protected abstract T getValidEntity();
 }
}