package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InformiBaseTest extends IzEntityTestBase<InformiBase> {

    //Default
    @Test
    public void whenValidInformi_thenDefaultValidatorSucceeds() {
        InformiBase informiBase = getValidEntity();

        Set<ConstraintViolation<InformiBase>> violations = validator.validate(informiBase);
        assertEquals(0, violations.size());
    }

    //Name         >>  !Blank
    @Test
    public void whenNameIsBlank_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();
        Set<ConstraintViolation<InformiBase>> violations;

        informiBase.setName("");
        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    //Description  >>  !Blank, <1500
    //!Blank
    @Test
    public void whenDescriptionIsBlank_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();
        Set<ConstraintViolation<InformiBase>> violations;

        informiBase.setDescription("");
        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    //>1500
    @Test
    public void whenDescriptionExceeds_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();

        informiBase.setDescription(RandomStringUtils.random(2560));
        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informiBase);
        assertEquals(1, violations.size());

    }


    @NotNull
    @Override
    protected InformiBase getValidEntity() {
        return getPopulatedInformi(1L);
    }
}
