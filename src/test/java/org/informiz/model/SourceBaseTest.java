package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedSourceBase;
import static org.informiz.model.SourceBase.SourceType.BLOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceBaseTest extends IzEntityTestBase<SourceBase> {


    //  Valid (Default, NewSource, ExistingSource)
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        SourceBase sourceBase = getValidEntity();

        Set<ConstraintViolation<SourceBase>> violations = validator.validate(sourceBase);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidSourceReference_thenNewSourceFromUIValidatorSucceeds() {
        SourceBase source = new SourceBase();

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        Set<ConstraintViolation<SourceBase>> violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
        assertEquals(0, violations.size());
    }

    @Test
    public void whenValidSourceReference_thenExistingSourceFromUIValidatorSucceeds() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestCitationIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(0, violations.size());
    }

    //Name   {!Blank}  (Default, NewSource, ExistingSource)
    @Test
    public void whenSourceNameIsBlank_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setName("");

        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenSourceNameIsBlank_thenNewSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setName("");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @Test
    public void whenSourceNameIsBlank_thenExistingSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestCitationIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setName("");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    // Link    {!Blank}  (Default, NewSource, ExistingSource)
    @Test
    public void whenLinkIsBlank_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setLink("");
        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsBlank_thenNewSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setName("Test SourceNName");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setLink("");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @Test
    public void whenLinkIsBlank_thenExistingSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestCitationIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setName("Test SourceNName");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setLink("");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    //  Link    {!Valid}  (Default, NewSource, ExistingSource)
    @Test
    public void whenLinkIsNotValid_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setLink("This Is Not Valid");

        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenLinkIsNotValid_thenNewSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setName("Test SourceNName");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setLink("This Is Not Valid");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @Test
    public void whenLinkIsNotValid_thenExistingSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestCitationIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setName("Test SourceNName");
        source.setSrcType(BLOG);
        source.setDescription("TestDescription Description");

        source.setLink("This Is Not Valid");

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    //SrcType    {!Null}  (Default, NewSource, ExistingSource)
    @Test
    public void whenSrcTypeIsNull_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setSrcType(null);

        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSrcTypeIsNull_thenNewSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setDescription("TestDescription Description");

        source.setSrcType(null);

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @Test
    public void whenSrcTypeIsNull_thenExistingSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestCitationIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setDescription("TestDescription Description");

        source.setSrcType(null);

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    //Description   {>500}  (Default, NewSource, ExistingSource)
    @Test
    public void whenDescriptionExceeds_thenDefaultValidatorViolation() {
        SourceBase sourceBase = getValidEntity();
        Set<ConstraintViolation<SourceBase>> violations;

        sourceBase.setDescription(RandomStringUtils.random(501));

        violations = validator.validate(sourceBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenDescriptionExceeds_thenNewSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);

        source.setDescription(RandomStringUtils.random(501));

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.NewSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @Test
    public void whenDescriptionExceeds_thenExistingSourceFromUIValidatorViolation() {
        SourceBase source = new SourceBase();

        source.setId(1l);
        source.setEntityId("TestEntityIdLongerThanTwentyFive");
        source.setOwnerId("1l");

        source.setName("Test SourceNName");
        source.setLink("http://www.server.com");
        source.setSrcType(BLOG);

        source.setDescription(RandomStringUtils.random(501));

        Set<ConstraintViolation<SourceBase>>
                violations = validator.validate(source, SourceBase.ExistingSourceFromUI.class);
                assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected SourceBase getValidEntity() {

        return getPopulatedSourceBase(1L);
    }
}