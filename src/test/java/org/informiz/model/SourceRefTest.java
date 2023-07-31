package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedSrcReference;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceRefTest extends IzEntityTestBase<SourceRef> {

    //Default
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        SourceRef sourceRef = getValidEntity();

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef);
        assertEquals(0, violations.size());
    }


    //SrcEntity >> =< 255
    @Test
    public void whenSrcRefExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }

    //SourcedId >>  !Blank, <256
    //!Blank
    @Test
    public void whenSourcedIdIsNull_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();
        Set<ConstraintViolation<SourceRef>> violations;

        sourceRef.setSourcedId("");
        violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }

    //<256
    @Test
    public void whenSourcedIdExeeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSourcedId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }


    //link <256
    @Test
    public void whenLinkExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }


    //description <256
    @Test
    public void whenDescriptionExeeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setDescription(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }


    @NotNull
    @Override
    protected SourceRef getValidEntity() {

        return getPopulatedSrcReference(1L);
    }
}
