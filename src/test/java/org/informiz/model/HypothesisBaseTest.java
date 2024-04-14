package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedHypothesis;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HypothesisBaseTest extends IzEntityTestBase<HypothesisBase> {

    //  Valid (Default, NewHypothesis, ExistingHypothesis)
    @Test
    public void whenValidHypothesis_thenDefaultValidatorSucceeds() {
        HypothesisBase hypothesisBase = getValidEntity();
        Set<ConstraintViolation<HypothesisBase>>

                violations = validator.validate(hypothesisBase);
                assertEquals(0, violations.size());
    }
    @Test
    public void whenValidHypothesis_thenNewHypothesisValidatorSucceeds() {
        HypothesisBase hypothesis = new HypothesisBase();

        hypothesis.setClaim("Hypothesised test hypothesis");

        Set<ConstraintViolation<HypothesisBase>>
                violations = validator.validate(hypothesis, HypothesisBase.NewHypothesisFromUI.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidHypothesis_thenExistingHypothesisValidatorSucceeds() {
        HypothesisBase hypothesis = new HypothesisBase();

        hypothesis.setId(1l);
        hypothesis.setEntityId("TestCitationIdLongerThanTwentyFive");
        hypothesis.setOwnerId("1l");

        hypothesis.setClaim("Hypothesised test hypothesis");

        Set<ConstraintViolation<HypothesisBase>>
                violations = validator.validate(hypothesis, HypothesisBase.ExistingHypothesisFromUI.class);
        assertEquals(0, violations.size());
    }

    //  Claim  {!Blank}  (Default, NewHypothesis, ExistingHypothesis)
    @Test
    public void whenHypothesisIsBlank_thenDefaultValidatorViolation() {
        HypothesisBase hypothesisBase = getValidEntity();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesisBase.setClaim("");

        violations = validator.validate(hypothesisBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenHypothesisIsBlank_thenNewHypothesisValidatorViolation() {
        HypothesisBase hypothesis = new HypothesisBase();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesis.setClaim("");

        violations = validator.validate(hypothesis, HypothesisBase.NewHypothesisFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenHypothesisIsBlank_thenExistingHypothesisValidatorViolation() {
        HypothesisBase hypothesis = new HypothesisBase();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesis.setId(1l);
        hypothesis.setEntityId("TestCitationIdLongerThanTwentyFive");
        hypothesis.setOwnerId("1l");

        hypothesis.setClaim("");

        violations = validator.validate(hypothesis, HypothesisBase.ExistingHypothesisFromUI.class);
        assertEquals(1, violations.size());
    }

    //Claim  {>500}  (Default, NewHypothesis, ExistingHypothesis)
    @Test
    public void whenHypothesisClaimExceeds_thenDefaultValidatorViolation() {
        HypothesisBase hypothesisBase = getValidEntity();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesisBase.setClaim(RandomStringUtils.random(501));

        violations = validator.validate(hypothesisBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenHypothesisClaimExceeds_thenNewHypothesisValidatorViolation() {
        HypothesisBase hypothesis = new HypothesisBase();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesis.setClaim(RandomStringUtils.random(501));

        violations = validator.validate(hypothesis, HypothesisBase.NewHypothesisFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenHypothesisClaimExceeds_thenExistingHypothesisValidatorViolation() {
        HypothesisBase hypothesis = new HypothesisBase();
        Set<ConstraintViolation<HypothesisBase>> violations;

        hypothesis.setId(1l);
        hypothesis.setEntityId("TestCitationIdLongerThanTwentyFive");
        hypothesis.setOwnerId("1l");

        hypothesis.setClaim(RandomStringUtils.random(501));

        violations = validator.validate(hypothesis, HypothesisBase.ExistingHypothesisFromUI.class);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected HypothesisBase getValidEntity() {
        return getPopulatedHypothesis(1L);
    }
}