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
import org.informiz.ctrl.citation.CitationController;
import org.informiz.ctrl.hypothesis.HypothesisController;
import org.informiz.model.CitationBase;
import org.informiz.model.HypothesisBase;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.hypothesis.HypothesisRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Map;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HypothesisController.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, HypothesisController.class, ErrorHandlingAdvice.class})
@Disabled("Validation doesn't work - FIX this")
class HypothesisControllerTest extends ControllerTest<HypothesisBase> {
    public static final String ALL_HYPOTHESIS_TITLE = "Factual claims, ranked for reliability";
    public static final String NEW_HYPOTHESIS = "New Claim";
    public static final String UPDATE_HYPOTHESIS = "Update Claim";
    public static final String DETAILS = "Details";
    public static final String CLAIM_SIZE = "Claim must be under 500 characters";
    @MockBean
    HypothesisRepository hypothesisRepository;

    @Override
    protected String prefix(){
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
    void whenMemberAddHypothesis_thenSucceeds() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                      //  "link", new String[]{"http://server.com"},
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        //"link", new String[]{"http://server.com"},
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddHypothesisClaimExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "claim", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }

    //Todo: Validation group doesn't include Id and OwnerId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenOwnerUpdatesHypothesis_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerUpdateHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(500)}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateHypothesisClaimExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1251"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "claim", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteHypothesis_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
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

        if(reviewOwnerId != null) {
            hypothesis.addReview(getPopulatedReview(hypothesis, reviewOwnerId));
        }
        return hypothesis;
    }
}