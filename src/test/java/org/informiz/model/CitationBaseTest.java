package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedCitation;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CitationBaseTest extends IzEntityTestBase<CitationBase> {

    //Valid   (Default, NewCitation, ExistingCitation)
    @Test
    public void whenValidCitation_thenDefaultValidatorSucceeds() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>>

                violations = validator.validate(citationBase);
                assertEquals(0, violations.size());
    }
    @Test
    public void whenValidCitation_thenNewCitationFromUIValidatorSucceeds() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setText("TestCitationIdLongerThanTwentyFive");
        citation.setLink("http://www.server.com");


        violations = validator.validate(citation, CitationBase.NewCitationFromUI.class);
        assertEquals(0, violations.size());
    }

    @Test
    public void whenValidCitation_thenExistingCitationFromUIValidatorSucceeds() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setId(1l);
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setOwnerId("1l");

        citation.setText("TestCitationIdLongerThanTwentyFive");
        citation.setLink("http://www.server.com");

        violations = validator.validate(citation, CitationBase.ExistingCitationFromUI.class);
        assertEquals(0, violations.size());
    }

    //Text  {!Blank}  (Default, NewCitation, ExistingCitation)
    @Test
    public void whenTextIsBlank_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setText("");

        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenTextIsBlank_thenNewCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setLink("http://www.server.com");

        citation.setText("");

        violations = validator.validate(citation, CitationBase.NewCitationFromUI.class);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenTextIsBlank_thenExistingCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setId(1l);
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setOwnerId("1l");

        citation.setLink("http://www.server.com");

        citation.setText("");
        violations = validator.validate(citation, CitationBase.ExistingCitationFromUI.class);
        assertEquals(1, violations.size());
    }

    //Text  {>500}  (Default, NewCitation, ExistingCitation)
    @Test
    public void whenTextExceeds_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setText(RandomStringUtils.random(501));

        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }

    @Test
    public void whenTextExceeds_thenNewCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setLink("http://www.server.com");

        citation.setText(RandomStringUtils.random(501));

        violations = validator.validate(citation, CitationBase.NewCitationFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenTextExceeds_thenExistingCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setId(1l);
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setOwnerId("1l");
        citation.setLink("http://www.server.com");

        citation.setText(RandomStringUtils.random(501));

        violations = validator.validate(citation, CitationBase.ExistingCitationFromUI.class);
        assertEquals(1, violations.size());
    }

    //Link  {!Blank}  (Default, NewCitation, ExistingCitation)
    @Test
    public void whenLinkIsBlank_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setLink("");
        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsBlank_thenNewCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setText("Citation validation groups Text testing");

        citation.setLink("");

        violations = validator.validate(citation, CitationBase.NewCitationFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsBlank_thenExistingCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setId(1l);
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setOwnerId("1l");
        citation.setText("TestCitationIdLongerThanTwentyFive");

        citation.setLink("");

        violations = validator.validate(citation, CitationBase.ExistingCitationFromUI.class);
        assertEquals(1, violations.size());
    }

    //Link  {!Valid}  (Default, NewCitation, ExistingCitation)
    @Test
    public void whenLinkIsNotValid_thenDefaultValidatorViolation() {
        CitationBase citationBase = getValidEntity();
        Set<ConstraintViolation<CitationBase>> violations;

        citationBase.setLink("This Is Not A Valid Link");

        violations = validator.validate(citationBase);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsNotValid_thenNewCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setText("Citation validation groups Text testing");

        citation.setLink("This Is Not A Valid Link");

        violations = validator.validate(citation, CitationBase.NewCitationFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsNotValid_thenExistingCitationFromUIValidatorViolation() {
        CitationBase citation = new CitationBase();
        Set<ConstraintViolation<CitationBase>> violations;

        citation.setId(1l);
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setOwnerId("1l");
        citation.setText("TestCitationIdLongerThanTwentyFive");

        citation.setLink("This Is Not A Valid Link");

        violations = validator.validate(citation, CitationBase.ExistingCitationFromUI.class);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected CitationBase getValidEntity() {

        return getPopulatedCitation(1L);
    }
}