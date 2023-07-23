package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedSrcReference;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class SourceRefTest extends IzEntityTestBase<SourceRef> {

    //Default
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        SourceRef sourceRef = getValidEntity();

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef);
        assertEquals(0, violations.size());
    }


    //SrcEntity < 256
    @Test
    public void whenSrcRefExeeds_thenUserReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        //Exceeds 255
        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }

    //sourcedId !null, <256
    //!Blank
    @Test
    public void whenSourcedIdIsNull_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();
        Set<ConstraintViolation<SourceRef>> violations;

        // !Blank
        sourceRef.setSourcedId("");
        violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }

    //<256
    @Test
    public void whenSourcedIdExeeds_thenUserReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        //Exceeds 255
        sourceRef.setSourcedId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }


    //link <256
    @Test
    @Disabled//I think my DataType is wrong
    public void whenLinkExeeds_thenUserReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        //Exceeds 255
        sourceRef.setLink(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }


    //description <256
    @Test
    public void whenDescriptionExeeds_thenUserReferenceValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        //Exceeds 255
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
