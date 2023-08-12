package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedHypothesis;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HypothesisBaseTest extends IzEntityTestBase<HypothesisBase> {
    //Default
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        HypothesisBase hypothesisBase = getValidEntity();

        Set<ConstraintViolation<HypothesisBase>> violations = validator.validate(hypothesisBase);
        assertEquals(0, violations.size());
    }

    //Claim  >>  !Blank
    //!Blank
    @Test
    public void whenHypothesisIsBlank_thenDefaultValidatorViolation() {
        HypothesisBase hypothesisBase = getValidEntity();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesisBase.setClaim("");
        violations = validator.validate(hypothesisBase);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected HypothesisBase getValidEntity() {
        return getPopulatedHypothesis(1L);
    }
}