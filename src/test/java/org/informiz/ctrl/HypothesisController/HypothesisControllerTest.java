package org.informiz.ctrl.HypothesisController;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ControllerTest;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.ctrl.hypothesis.HypothesisController;
import org.informiz.model.HypothesisBase;
import org.informiz.model.Reference;
import org.informiz.model.SourceRef;
import org.informiz.repo.hypothesis.HypothesisRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Map;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HypothesisController.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, HypothesisController.class, ErrorHandlingAdvice.class})
//@Disabled("Validation doesn't work - FIX this")
class HypothesisControllerTest extends ControllerTest<HypothesisBase> {
    public static final String ALL_HYPOTHESIS_TITLE = "Factual claims, ranked for reliability";
    public static final String NEW_HYPOTHESIS = "New Claim";
    public static final String UPDATE_HYPOTHESIS = "Update Claim";
    public static final String DETAILS = "Details";
    public static final String CLAIM_SIZE = "Claim must be under 500 characters";
    @MockitoBean
    HypothesisRepository hypothesisRepository;

    @Override
    protected String prefix() {
        return "hypothesis";
    }

    @Override
    protected String allEntitiesTitle() {
        return ALL_HYPOTHESIS_TITLE;
    }

    @Override
    protected String newEntityTitle() {
        return NEW_HYPOTHESIS;
    }

    @Override
    protected String updateEntityTitle() {
        return UPDATE_HYPOTHESIS;
    }

    @Override
    protected String viewEntityTitle() {
        return DETAILS;
    }

    @Override
    protected String textExceedsMsg() {
        return CLAIM_SIZE;
    }

    protected String EntityIllegalArgumentTitle() {
        return "Illegal argument, an error was logged and will be addressed by a developer";
    }

    @Override
    protected String entityReviewUrl() {
        return "/hypothesis/details/1";
    }

    //Todo: disable log
    //Todo: Fix Error Msg
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsHypothesisInvalidId_thenErrorMsg() throws Exception {
        verifyGetApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(EntityIllegalArgumentTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberAddValidHypothesis_thenSucceeds() throws Exception {
    //Todo: Is there any limit to preemptive forward slashes in the path?
        verifyPostApiCall( "///add", Map.of(
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsValidHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall("add", Map.of(
                        //  "link", new String[]{"http://server.com"},
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall("add", Map.of(
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddHypothesisClaimExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall("add", Map.of(
                        "claim", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }

    //Todo: Validation group doesn't include Id and OwnerId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenOwnerUpdatesValidHypothesis_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1", Map.of(
                        "id", new String[]{"1"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @Disabled ("Unable to test mock content. These are controller tests")
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenOwnerUpdatesHypothesisContent_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null);
        verifyPostApiCall(populatedEntity2, "details/1", Map.of(
                        "id", new String[]{"1"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{"musk's_WaterXPee"}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));

        verifyGetApiCall(populatedEntity2, "/view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("musk's_WaterXPee"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerUpdateHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(500)}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateHypothesisClaimExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1", Map.of(
                        "id", new String[]{"69"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberDeleteHypothesis_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerDeleteHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }


    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidAddReference_thenSucceeds() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "entailment", new String[]{"SUPPORTS"},
                        "degree", new String[]{"0.9f"},
                        "comment", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }
    //TODO: Seems like the browser sends unnecessary fields (CheckerId)

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddReference_thenForbidden() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "entailment", new String[]{"SUPPORTS"},
                        "degree", new String[]{"0.9f"},
                        "comment", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceBlankFactCheckedEntityId_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{""},
                        "refEntityId", new String[]{("test1")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceFactCheckedEntityIdExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{RandomStringUtils.random(256)},
                        "refEntityId", new String[]{"test1"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceBlankRefEntityId_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be blank"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceRefEntityIdExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceNullEntailment_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "entailment", new String[]{null}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be null"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceDegreeGreaterThanOne_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{"1.9f"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must be less than or equal to 1.0"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceNullDegree_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{null}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be null"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceCommentExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1", Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "comment", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerEditValidReference_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);

        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "id", new String[]{"1"},
                        "refEntityId", new String[]{"entityId_of_some_citation"},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "entailment", new String[]{"SUPPORTS"}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerEditReferenceComment_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);

        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "id", new String[]{"1"},
                        "refEntityId", new String[]{"entityId_of_some_citation"},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{"Ammona make it foshow!"},
                        "entailment", new String[]{"SUPPORTS"}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));

        verifyGetApiCall(populatedEntity2, "/details/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("foshow!"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId = "Some_Checker")
    void whenNotOwnerEditReference_thenForbidden() throws Exception {
        HypothesisBase populatedEntity = getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID);
        Reference ref = new Reference();
        populatedEntity.addReference(ref);
        verifyPostApiCall(populatedEntity, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditReference_thenForbidden() throws Exception {
        HypothesisBase populatedEntity = getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID);
        Reference ref = new Reference();
        populatedEntity.addReference(ref);
        verifyPostApiCall(populatedEntity, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "entailment", new String[]{"SUPPORTS"},
                        "degree", new String[]{"0.9f"},
                        "comment", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerEditReferenceBlankRefEntityId_thenErrorMsg() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);

        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReferenceNullDegree_thenErrorMsg() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{null}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be null"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReferenceRefEntityIdExceeds_thenErrorMsg() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{RandomStringUtils.random(256)},
                        "entailment", new String[]{"SUPPORTS"},
                        "comment", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReferenceCommentExceeds_thenErrorMsg() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "comment", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidDeletesReference_thenSucceed() throws Exception {

        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/ref/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}
                ),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerDeletesReference_thenForbidden() throws Exception {

        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/ref/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId = "Some_Checker")
    void whenNotOwnerDeletesReference_thenForbidden() throws Exception {

        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = getPopulatedReference(populatedEntity2);
        populatedEntity2.addReference(ref);
        verifyPostApiCall(populatedEntity2, "/reference/1/ref/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}
                ),
                Arrays.asList(status().isForbidden()));
    }


    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidAddSourceRef_thenSucceeds() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{("6969")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddSourceRef_thenForbidden() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{("6969")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddSourceRefBlankSourcedId_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{""},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddSourceRefSourcedIdExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{RandomStringUtils.random(256)},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddSourceRefInvalidLink_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{("6969")},
                        "link", new String[]{"test link"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must be a valid URL"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddSourceRefLinkExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{("6969")},
                        "link", new String[]{RandomStringUtils.random(256)},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddSourceRefDescriptionExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/source-ref/1", Map.of(
                        "sourcedId", new String[]{("6969")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidEditSourceRef_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/edit/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{("691169")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    //ContentTesting
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidEditSourceRefDescription_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/edit/", Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{("1")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{"Sarah_Connor"}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));

        verifyGetApiCall(populatedEntity2, "/details/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Sarah_Connor"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId = "Some_Checker")
    void whenNotOwnerEditSourceRef_thenForbidden() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/edit/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{("691169")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditSourceRef_thenForbidden() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/edit/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{("691169")},
                        "link", new String[]{"https://server.com/"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditSourceRefInvalidLink_thenErrorMsg() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/edit/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{("691169")},
                        "link", new String[]{"Test link"},
                        "description", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must be a valid URL"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidDeleteSourceRef_thenSucceeds() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId = "Some_Checker")
    void whenNotOwnerDeleteSourceRef_thenForbidden() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerDeleteSourceRef_thenForbidden() throws Exception {
        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        SourceRef sorC = getPopulatedSourceRef(populatedEntity2);
        populatedEntity2.addSource(sorC);
        verifyPostApiCall(populatedEntity2, "/source-ref/1/del/", Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "sourcedId", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isForbidden()));
    }


    @Override
    @NotNull
    protected HypothesisBase getPopulatedEntity(String ownerId, String reviewOwnerId) {
        HypothesisBase hypothesis = new HypothesisBase();
        hypothesis.setLocalId(1l);
        hypothesis.setId(1l);
        hypothesis.setEntityId(TEST_ENTITY_ID);
        hypothesis.setCreatorId("test");
        hypothesis.setOwnerId(ownerId);
        hypothesis.setCreatedTs(12345l);
        hypothesis.setUpdatedTs(12345l);
        //hypothesis.setSources();
        //hypothesis.setReferences("https://informiz.org");
        hypothesis.setClaim("Test hypothesis");

        if (reviewOwnerId != null) {
            hypothesis.addReview(getPopulatedReview(hypothesis, reviewOwnerId));
        }
        return hypothesis;
    }

//    @Override
    @NotNull
    protected Reference getPopulatedReference (HypothesisBase populatedEntity) {
//        HypothesisBase populatedEntity2 = getPopulatedEntity("some owner", null);
        Reference ref = new Reference();
        ref.setId(1l);
        ref.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        ref.setCreatorId(DEFAULT_TEST_CHECKER_ID);
        ref.setFactCheckedEntityId(TEST_ENTITY_ID);
        ref.setRefEntityId("entityId_of_some_citation");
        ref.setEntailment(Reference.Entailment.SUPPORTS);
        ref.setDegree(0.69f);
        ref.setComment("Ammona make it!");
//        populatedEntity2.addReference(ref);

        return ref;
    }

    protected SourceRef getPopulatedSourceRef (HypothesisBase populatedEntity) {
        SourceRef sorC = new SourceRef();
        sorC.setSourcedId("1");
        sorC.setSrcEntityId("entityId_of_some_sourceRef");
        sorC.setLink("http://informiz.org");
        sorC.setDescription("Who will save us?");
        sorC.setId(1L);
        sorC.setCreatorId(DEFAULT_TEST_CHECKER_ID);
        sorC.setOwnerId(DEFAULT_TEST_CHECKER_ID);


        return sorC;
    }
}