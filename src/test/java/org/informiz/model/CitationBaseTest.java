package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedCitation;
import static org.informiz.model.ModelTestUtils.getPopulatedHypothesis;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CitationBaseTest extends IzEntityTestBase<CitationBase>{

    //Default
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        CitationBase citationBase = getValidEntity();

        Set<ConstraintViolation<CitationBase>> violations = validator.validate(citationBase);
        assertEquals(0, violations.size());
    }

    //Text  >> !Blank, =<500
    //!Blank
    @Test
    public void whenTextIsNull_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setText("");
        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    // =<500
    @Test
    public void whenTextExceeds_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setText(RandomStringUtils.random(501));
        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    //Link  >>  !Blank, !Valid
    //!Blank
    @Test
    public void whenLinkNull_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setLink("");
        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    //!Valid
    @Test
    public void whenLinkIsNotValid_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setLink("This Is Not Valid");
        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected CitationBase getValidEntity() {

        return getPopulatedCitation(1L);
    }
}