package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.informiz.model.IzEntityTestBase;
import org.informiz.model.SourceRef;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.informiz.model.ModelTestUtils.getPopulatedSrcReference;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
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
        // not null
        informiBase.setName("");
        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    //description  >>  !Blank, <1500
    //!Blank
    @Test
    public void whenDescriptionIsBlank_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();
        Set<ConstraintViolation<InformiBase>> violations;
        // not null
        informiBase.setDescription("");
        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    //>1500
    @Test
    public void whenDescriptionExceeds_thenUserReferenceValidatorViolation() {
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
