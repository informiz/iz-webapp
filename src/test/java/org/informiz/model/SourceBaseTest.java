package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedSourceBase;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceBaseTest extends IzEntityTestBase<SourceBase>{

    //Default
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        SourceBase sourceBase = getValidEntity();

        Set<ConstraintViolation<SourceBase>> violations = validator.validate(sourceBase);
        assertEquals(0, violations.size());
    }

    //Name    >> !Blank
    //!Blank
    @Test
    public void whenSourceBaseNull_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setName("");
        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }

    //Link    >> !Blank, ValidURL
    //!Blank
    @Test
    public void whenLinkNull_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setLink("");
        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }

    //!Valid
    @Test
    public void whenLinkIsNotValid_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setLink("This Is Not Valid");
        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }


    //SrcType >> !Null
    //!Null
    @Test
    public void whenSrcTypeIsNull_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setSrcType(null);
        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected SourceBase getValidEntity() {

        return getPopulatedSourceBase(1L);
    }
}