package org.informiz.ctrl.citation;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.model.CitationBase;
import org.informiz.repo.citation.CitationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Map;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitationController.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, CitationController.class, ErrorHandlingAdvice.class})

class CitationControllerTest extends org.informiz.ctrl.ControllerTest<CitationBase> {
    public static final String ALL_CITATIONS_TITLE = "Quotes from e.g people, books or articles, ranked for reliability";
    public static final String NEW_CITATION = "New Citation";
    public static final String UPDATE_CITATION = "Update Citation";
    public static final String DETAILS = "Details";
    public static final String SIZE_MUST_BE_BETWEEN_0_AND_500 = "size must be between 0 and 500";
    public static final String INVALID_LINK = "Please provide a link to the source of the citation";

    public static final String COMMENT_SIZE = "Comment must be under 255 characters";
    @MockBean
    CitationRepository citationRepository;

    @Override
    protected String prefix(){
        return "citation";
    }

    @Override
    protected String allEntitiesTitle() {
        return ALL_CITATIONS_TITLE;
    }

    @Override
    protected String newEntityTitle() {
        return NEW_CITATION;
    }
    @Override
    protected String updateEntityTitle() {
        return UPDATE_CITATION;
    }
    //@Override
    protected String addEntityUrl() {
        return "/citation/all";
    }
    @Override
    protected String viewEntityTitle() {
        return DETAILS;
    }
    @Override
    protected String textExceedsMsg() {
        return SIZE_MUST_BE_BETWEEN_0_AND_500;
    }
    protected String commentExceedsMsg() {
        return COMMENT_SIZE;
    }
    protected String EntityIllegalArgumentTitle() {
        return "Illegal argument, an error was logged and will be addressed by a developer";

    }
    //@Override
    protected String invalidCitationLinkMsg() {
        return INVALID_LINK;
    }
    @Override
    protected String deleteEntityUrl() {
        return "/citation/all";
    }
    //
    @Override
    protected String entityReviewUrl() {
        return "/citation/details/1";
    }

    //Todo: disable log
    //Todo: Fix Error Msg
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsCitationInvalidId_thenErrorMsg() throws Exception {

        verifyGetApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(EntityIllegalArgumentTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberAddCitation_thenSucceeds() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isFound(), redirectedUrl(addEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsCitation_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddCitation_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenCitationURLisInvalid_thenErrorMsg() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "link", new String[]{"Invalid"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(invalidCitationLinkMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddCitationTextExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerUpdateCitation_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "text", new String[]{RandomStringUtils.random(500)}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateCitationInvalidLink_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "link", new String[]{"Invalid"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(invalidCitationLinkMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateCitationTextExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteCitation_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }

    @Override
    @NotNull
    protected CitationBase getPopulatedEntity(String ownerId, String reviewOwnerId) {
        CitationBase citation = new CitationBase();
        citation.setLocalId(1l);
        citation.setEntityId(TEST_ENTITY_ID);
        citation.setCreatorId("test");
        citation.setOwnerId(ownerId);
        citation.setCreatedTs(12345l);
        citation.setUpdatedTs(12345l);
        citation.setLink("https://informiz.org");
        citation.setText("Test citation");

        if(reviewOwnerId != null) {
            citation.addReview(getPopulatedReview(citation, reviewOwnerId));
        }
        return citation;
    }
}