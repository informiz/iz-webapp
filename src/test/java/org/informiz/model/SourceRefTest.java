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


    //SrcEntityId {Default} >> =< 255
    @Test
    public void whenSrcEntityIdExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }

    //SrcEntityId {DeleteEntity} >> =< 255
    @Test
    public void whenSrcEntityIdExceeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

    }

    //SrcEntityId {UserSource} >> =< 255
    @Test
    public void whenSrcEntityIdExceeds_thenUserSourceReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, SourceRef.UserSourceReference.class);
        assertEquals(1, violations.size());

    }

    //SourcedEntityId {Default} >>  !Blank, <256
    //!Blank
    @Test
    public void whenSourcedEntityIdIsBlank_thenDefaultValidatorViolation() {
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

    //SourcedEntityId {DeleteEntity} >>  !Blank, <256
    //!Blank
    @Test
    public void whenSourcedEntityIdIsBlank_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = getValidEntity();
        Set<ConstraintViolation<SourceRef>> violations;

        sourceRef.setSourcedId("");
        violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //<256
    @Test
    public void whenSourcedIdExeeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSourcedId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

    }


    //SourcedEntityId {UserSourceReference} >>  !Blank, <256
    //!Blank
    @Test
    public void whenSourcedEntityIdIsBlank_thenUserSourceReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();
        Set<ConstraintViolation<SourceRef>> violations;

        sourceRef.setSourcedId("");
        violations = validator.validate(sourceRef, SourceRef.UserSourceReference.class);
        assertEquals(1, violations.size());
    }

    //<256
    @Test
    public void whenSourcedIdExeeds_thenUserSourceReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSourcedId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, SourceRef.UserSourceReference.class);
        assertEquals(1, violations.size());

    }


    //Link {Default} >> URL<255
    //link <256
    @Test
    public void whenLinkExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }

    //Link {DeleteEntity} >> URL<255
    //link <256
    @Test
    public void whenLinkExceeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

    }


    //Link {UserSourceReference} >> URL<255
    //link <256
    @Test
    public void whenLinkExceeds_thenUserSourceReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef, SourceRef.UserSourceReference.class);
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
