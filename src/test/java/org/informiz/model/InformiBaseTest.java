package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InformiBaseTest extends IzEntityTestBase<InformiBase> {

    //  Valid (Default, NewInformi, ExistingInformi)
    @Test
    public void whenValidInformi_thenDefaultValidatorSucceeds() {
        InformiBase informiBase = getValidEntity();

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informiBase);
                assertEquals(0, violations.size());
    }
    @Test
    public void whenValidInformi_thenNewInformiFromUIValidatorSucceeds() {
        InformiBase informi = new InformiBase();

        informi.setName("1l");
        informi.setDescription("Test description");
        informi.setMediaPath("http://www.server.com");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.NewInformiFromUI.class);
                assertEquals(0, violations.size());
    }

    @Test
    public void whenValidInformi_thenExistingInformiFromUIValidatorSucceeds() {
        InformiBase informi = new InformiBase();

        informi.setId(1l);
        informi.setEntityId("TestEntityIdLongerThanTwentyFive");
        informi.setOwnerId("1l");

        informi.setName("1l");
        informi.setDescription("Test description");
        informi.setMediaPath("http://www.server.com");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.ExistingInformiFromUI.class);
                assertEquals(0, violations.size());
    }

    //Name   {!Blank}   (Default, NewInformi, ExistingInformi)
    @Test
    public void whenNameIsBlank_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();
        Set<ConstraintViolation<InformiBase>> violations;

        informiBase.setName("");

        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenNameIsBlank_thenNewInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setDescription("Test description");
        informi.setMediaPath("http://www.server.com");

        informi.setName("");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.NewInformiFromUI.class);
                assertEquals(1, violations.size());
    }
    @Test
    public void whenNameIsBlank_thenExistingInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setId(1l);
        informi.setEntityId("TestEntityIdLongerThanTwentyFive");
        informi.setOwnerId("1l");

        informi.setDescription("Test description");
        informi.setMediaPath("http://www.server.com");

        informi.setName("");
        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.ExistingInformiFromUI.class);
                assertEquals(1, violations.size());
    }

    //Description  {!Blank}  (Default, NewInformi, ExistingInformi)
    @Test
    public void whenDescriptionIsBlank_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();
        Set<ConstraintViolation<InformiBase>> violations;

        informiBase.setDescription("");

        violations = validator.validate(informiBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenDescriptionIsBlank_thenNewInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setMediaPath("http://www.server.com");
        informi.setName("InformiTest");

        informi.setDescription("");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.NewInformiFromUI.class);
                assertEquals(1, violations.size());
    }
    @Test
    public void whenDescriptionIsBlank_thenExistingInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setId(1l);
        informi.setEntityId("TestEntityIdLongerThanTwentyFive");
        informi.setOwnerId("1l");

        informi.setMediaPath("http://www.server.com");
        informi.setName("InformiTest");

        informi.setDescription("");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.ExistingInformiFromUI.class);
                assertEquals(1, violations.size());
    }

    //Description  {>1500}  (Default, NewInformi, ExistingInformi)
    @Test
    public void whenDescriptionExceeds_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();

        informiBase.setDescription(RandomStringUtils.random(1501));

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informiBase);
                assertEquals(1, violations.size());
    }


    @Test
    public void whenDescriptionExceeds_thenNewInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setMediaPath("http://www.server.com");
        informi.setName("InformiTest");

        informi.setDescription(RandomStringUtils.random(1501));

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.NewInformiFromUI.class);
                assertEquals(1, violations.size());
    }
    @Test
    public void whenDescriptionExceeds_thenExistingInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setId(1l);
        informi.setEntityId("TestEntityIdLongerThanTwentyFive");
        informi.setOwnerId("1l");

        informi.setMediaPath("http://www.server.com");
        informi.setName("InformiTest");

        informi.setDescription(RandomStringUtils.random(1501));

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.ExistingInformiFromUI.class);
                assertEquals(1, violations.size());
    }

    //MediaPath  {!ValidURL}  (Default, NewInformi, ExistingInformi)
    @Test
    public void whenMediaPathIsNotValid_thenDefaultValidatorViolation() {
        InformiBase informiBase = getValidEntity();

        informiBase.setMediaPath("This is NOT a valid URL");

        Set<ConstraintViolation<InformiBase>>
            violations = validator.validate(informiBase);
            assertEquals(1, violations.size());
    }

    @Test
    public void whenMediaPathIsNotValid_thenNewInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setDescription(RandomStringUtils.random(1500));
        informi.setName("InformiTest");

        informi.setMediaPath("This is NOT a valid URL");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.NewInformiFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenMediaPathIsNotValid_thenExistingInformiFromUIValidatorViolation() {
        InformiBase informi = new InformiBase();

        informi.setId(1l);
        informi.setEntityId("TestEntityIdLongerThanTwentyFive");
        informi.setOwnerId("1l");

        informi.setDescription(RandomStringUtils.random(1500));
        informi.setName("InformiTest");

        informi.setMediaPath("This is NOT a valid URL");

        Set<ConstraintViolation<InformiBase>>
                violations = validator.validate(informi, InformiBase.ExistingInformiFromUI.class);
                assertEquals(1, violations.size());
    }


    @NotNull
    @Override
    protected InformiBase getValidEntity() {

        return getPopulatedInformi(1L);
    }
}
