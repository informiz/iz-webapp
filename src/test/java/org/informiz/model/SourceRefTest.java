package org.informiz.model;

import jakarta.validation.ConstraintViolation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import static org.informiz.model.ModelTestUtils.getPopulatedSrcReference;
import static org.informiz.model.ModelTestUtils.getValidUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceRefTest extends IzEntityTestBase<SourceRef> {

    //  Valid (Default, NewUsrRef, ExistingUsrRef, ExistingNTT,DeleteNTT)
    @Test
    public void whenValidSourceReference_thenDefaultValidatorSucceeds() {
        SourceRef sourceRef = getValidEntity();

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidSourceReference_thenNewUserSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setSourcedId("Testing_SourcedID_String");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidSourceReference_thenExistingUserSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("1l");

        sourceRef.setSourcedId("Testing_SourcedID_String");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidSourceReference_thenExistingEntityFromUIValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenValidSourceReference_thenDeleteEntityValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);

        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId("Testing_SourcedID_String");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(0, violations.size());
    }

    // ID {!Null}  (Default, ExistingNTT, DeleteNTT)
    @Test
    public void whenSourceReferenceIdIsNull_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setId(null);

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourceReferenceIdIsNull_thenExistingEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setId(null);

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourceReferenceIdIsNull_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setOwnerId("TestOwnerId");
        sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setId(null);

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    // ID {Positive}  (Default, ExistingNTT, DeleteNTT)
    @Test
    public void whenSourceReferenceIdIsNegative_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setId(-1L);

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourceReferenceIdIsNegative_thenExistingEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setId(-1L);

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.ExistingEntityFromUI.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourceReferenceIdIsNegative_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setOwnerId("TestOwnerId");
        sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setId(-1L);

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }


    //SrcEntityId {<255}  (Default, NewUsrRef, ExistingUsrRef, DeleteNTT)
    @Test
    public void whenSrcEntityIdExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));
        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }
    @Test
    public void whenSrcEntityIdExceeds_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        //sourceRef.setOwnerId("TestOwnerId");
        sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSrcEntityIdExceeds_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSrcEntityIdExceeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        sourceRef.setSourcedId("Testing_SourcedID_String");

        sourceRef.setSrcEntityId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());

    }

    //SourcedId {!Blank}  (Default, NewUsrRef, ExistingUsrRef, DeleteNTT)
    @Test
    public void whenSourcedIdIsBlank_thenDefaultValidatorSucceeds() {
        SourceRef sourceRef = getValidEntity();
        Set<ConstraintViolation<SourceRef>> violations;

        sourceRef.setSourcedId("");
        violations = validator.validate(sourceRef);
        assertEquals(0, violations.size());
    }
    @Test
    public void whenSourcedIdIsBlank_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        //sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId("");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourcedIdIsBlank_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId("");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourcedIdIsBlank_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSrcEntityId(RandomStringUtils.random(255));

        sourceRef.setSourcedId("");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //SourcedId {<255}  (Default, NewUsrRef, ExistingUsrRef, DeleteNTT)
    @Test
    public void whenSourcedIdExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setSourcedId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());

    }
    @Test
    public void whenSourcedIdExceeds_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        //sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourcedIdExceeds_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenSourcedIdExceeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSrcEntityId(RandomStringUtils.random(255));

        sourceRef.setSourcedId(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }


    //  Link {Valid URL}   (Default, NewSrcRef, ExistingSrcRef, DeleteNTT)
    @Test
    public void whenLinkIsInvalid_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("Testing an invalid address");

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsInvalid_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        //sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("Testing an invalid address");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsInvalid_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("Testing an invalid address");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkIsInvalid_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSrcEntityId(RandomStringUtils.random(255));

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("Testing an invalid address");

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //  Link {<255}   (Default, NewSrcRef, ExistingSrcRef, DeleteNTT)
    @Test
    public void whenLinkExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkExceeds_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        //sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkExceeds_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenLinkExceeds_thenDeleteEntityValidatorViolation() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setOwnerId("TestOwnerId");
        //sourceRef.setSrcEntityId(RandomStringUtils.random(255));

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        sourceRef.setLink("https://server.com/" + RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, InformizEntity.DeleteEntity.class);
        assertEquals(1, violations.size());
    }

    //description {<255}   (Default, NewSrcRef, ExistingSrcRef)
    @Test
    public void whenDescriptionExceeds_thenDefaultValidatorViolation() {
        SourceRef sourceRef = getValidEntity();

        sourceRef.setDescription(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>>
                violations = validator.validate(sourceRef);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenDescriptionExceeds_thenNewSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        //sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        //sourceRef.setLink("https://server.com/");
        sourceRef.setDescription(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.NewUserSourceReference.class);
        assertEquals(1, violations.size());
    }
    @Test
    public void whenDescriptionExceeds_thenExistingSourceReferenceValidatorSucceeds() {
        SourceRef sourceRef = new SourceRef();

        sourceRef.setId(1l);
        sourceRef.setSrcEntityId(RandomStringUtils.random(255));
        sourceRef.setOwnerId("TestOwnerId");

        sourceRef.setSourcedId(RandomStringUtils.random(255));

        //sourceRef.setLink("https://server.com/");
        sourceRef.setDescription(RandomStringUtils.random(256));

        Set<ConstraintViolation<SourceRef>> violations = validator.validate(sourceRef, SourceRef.ExistingUserSourceReference.class);
        assertEquals(1, violations.size());
    }

    @NotNull
    @Override
    protected SourceRef getValidEntity() {

        return getPopulatedSrcReference(1L);
    }
}
