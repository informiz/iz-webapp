package org.informiz.ctrl.citation;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.CitationBase;
import org.informiz.model.Review;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.source.SourceRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, CitationController.class, ErrorHandlingAdvice.class})
@ActiveProfiles("test")
@WebMvcTest(CitationController.class)
class CitationControllerTest {
    @MockBean
    private CitationRepository repo;

    @MockBean
    private SourceRepository sourceRepo; // citation controller requires source repo

    @MockBean
    private SecurityConfig.ClientIdService googleOAuthService;

    @Autowired
    private MockMvc mockMvc;

    protected String prefix(){
        return "citation";
    }

    //View Citation (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsCitation_thenAllowed() throws Exception {
        CitationBase citation = getPopulatedEntity();
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        verifyActionNoParamApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Details"))));
    }

    //View Citation (Invalid LocalId) Todo: disable log
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsCitationInvalidId_thenErrorMsg() throws Exception {

        verifyActionNoParamApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Illegal argument, an error was logged and will be addressed by a developer"))));
    }
    //Add Citation Form (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsAddCitationForm_thenAllowed() throws Exception {

        verifyActionNoParamApiCall("add",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("New Citation"))));
    }
    //Add Citation Form (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsAddCitationForm_thenForbidden() throws Exception {

        verifyActionNoParamApiCall("add",
                Arrays.asList(status().isForbidden()));
    }
    //Get Citations (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsCitations_thenAllowed() throws Exception {

        verifyActionNoParamApiCall("all",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Citations"))));
    }
    //Edit Citation (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsEditCitation_thenForbidden() throws Exception {
        CitationBase citation = getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        verifyActionNoParamApiCall("details/1",
                Arrays.asList(status().isForbidden()));
    }
    //Get All Citations (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsAllCitation_thenAllowed() throws Exception {

        verifyActionNoParamApiCall("all",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Citations"))));
    }
    //View Add-Citation FORM (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsAddCitation_thenAllowed() throws Exception {

        verifyActionNoParamApiCall("add",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("New Citation"))));
    }
    //Add Citation (Viewer Forbidden)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsCitation_thenForbidden() throws Exception {

        verifyActionWithParamApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }
    //Add Citation (Fact checker forbidden)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddCitation_thenForbidden() throws Exception {

        verifyActionWithParamApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));//No content
    }
    //Add Citation (Text Exceeds 500)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddCitationTextExceeds_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall("add",  Map.of(
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 500"))));
    }
    //Add Citation (Invalid URL)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenCitationURLisInvalid_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall("add",  Map.of(
                        "link", new String[]{"Invalid"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please provide a link to the source of the citation"))));
    }

    //Update Citation (get, No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenValidUpdateCitation_thenSucceeds() throws Exception {
        CitationBase citation = getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        verifyActionNoParamApiCall("details/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Update Citation"))));
    }
    //Update Citation (Invalid OwnerID)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenInvalidCitationOwnerId_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "link", new String[]{"Invalid"}
                //"ownerId", new String[]{citation.getOwnerId()}
                ),
                Arrays.asList(status().isForbidden()));

    }
    //Update Citation (Invalid Link)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    @Disabled("Does not work if user doesn't send id")
    void whenUpdateCitationInvalidLink_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        //"id", new String[]{citation.getLocalId().toString()},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "link", new String[]{"Invalid"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please provide a link to the source of the citation"))));
    }
    //Update Citation (Exceeds 500)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    @Disabled("Does not work if user doesn't send id")
    void whenUpdateCitationTextExceeds_thenErrorMsg() throws Exception {



        verifyActionWithParamApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "link", new String[]{"http://server.com"},
                        "text", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 500"))));
    }
    //Delete Citation(No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenValidDeleteCitation_thenSucceeds() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isFound()));
    }
    //Delete Citation(Not owner)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenNotOwnerDeleteCitation_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", null), "delete/1",  Map.of(
                        "ownerId", new String[]{"some checker"}),
                Arrays.asList(status().isForbidden()));
    }
    //Add a valid Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddValidReview_thenSucceeds() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", null), "1/review/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isFound(), redirectedUrl("/citation/details/1")));


    }
    //Add Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddCitationReview_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", null), "1/review/edit/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isForbidden()));
    }
    //Add Review (OwnerId)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddReviewOfCitation_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", null), "1/review/edit/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isForbidden()));
    }
    //Add Review (No Rating)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReviewNoRating_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/",  Map.of(
                       // "rating", new String[]{"0.82"}
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please submit rating between 0.0 and 1.0"))));
    }
    //Add Review Comment Exceeds
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenReviewCommentExceeds_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/",  Map.of(
                        "rating", new String[]{"0.82"},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(256)}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Comment must be under 255 characters"))));
    }
    //Edit Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidEditReview_thenSucceeds() throws Exception {

        CitationBase citation = getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        verifyActionWithParamApiCall("1/review/edit/",  Map.of(
                        "id", new String[]{citation.getLocalId().toString()},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{(citation.getEntityId())}
                ),
                Arrays.asList(status().isFound()));
    }
    //Edit Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditsReviewOfCitation_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                ),
                Arrays.asList(status().isForbidden()));
    }
    //Edit Review (OwnerId)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId="Some_Checker")
    void whenNotOwnerEditCitationReview_thenForbidden() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID}
                ),
                Arrays.asList(status().isForbidden()));

    }
    //Edit Review No Rating
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewNoRating_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}
                        //"rating", new String[]{("0.82")},
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please submit rating between 0.0 and 1.0"))));
    }
    //Edit Review Comment Exceeds 255
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewCommentExceeds255_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{(DEFAULT_TEST_CHECKER_ID)},
                        "comment", new String[]{RandomStringUtils.random(256)}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Comment must be under 255 characters"))));
    }
    //Delete Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenDeletesReviewOfCitation_thenSucceed() throws Exception {

        CitationBase citation = getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/del/",  Map.of(
                        "id", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{(citation.getEntityId())}
                ),
                Arrays.asList(status().isFound()));
    }

    //Delete Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerNotOwnerDeletesCitationReview_thenForbidden() throws Exception {


       verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID),
               "/1/review/del/",  Map.of(
                        "ownerId", new String[]{"some checker"},
                       "id", new String[]{"1"},
                       "reviewedEntityId", new String[]{TEST_ENTITY_ID}),

                Arrays.asList(status().isForbidden()));
    }
    //Delete Review (No Id)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenDeleteEntityReviewNoId_thenErrorMessage() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID),
                "1/review/del/",  Map.of(
                        //"id", new String[]{citation.getLocalId().toString()},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{("test")}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please provide an ID"))));

    }
    //Edit Review (Invalid ReviewedEntityId)
    @Test
    @Disabled("Currently no error message for reviewedEntityId")
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewBlankReviewedEntityId_thenErrorMsg() throws Exception {

        verifyActionWithParamApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{("")}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("No Error MSG"))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    protected void verifyActionNoParamApiCall(String path, List<ResultMatcher> matchers) throws Exception {
        performRequest(get(String.format("/%s/%s", prefix(), path)), Map.of(),
                matchers);
    }

    protected void verifyActionWithParamApiCall(String path, Map<String, String[]> params, List<ResultMatcher> matchers) throws Exception {
        performRequest(post(String.format("/%s/%s", prefix(), path)), params,
                matchers);
    }
    protected void verifyActionWithParamApiCall(CitationBase citation, String path, Map<String, String[]> params, List<ResultMatcher> matchers) throws Exception{
        if(citation != null){
            given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));
        }
        performRequest(post(String.format("/%s/%s", prefix(), path)), params, matchers);
    }


    protected void performRequest(@NotNull MockHttpServletRequestBuilder request, Map<String, String[]> params, List<ResultMatcher> matchers) throws Exception {
        if (params != null && !params.isEmpty())
            params.entrySet().forEach(entry -> request.param(entry.getKey(), entry.getValue()));

        ResultActions resultActions = mockMvc.perform(request
                .secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        if (matchers != null && !matchers.isEmpty())
            resultActions.andExpectAll(matchers.toArray(new ResultMatcher[matchers.size()]));
    }




    @NotNull
    protected CitationBase getPopulatedEntity() {
        return getPopulatedEntity("test", null);
    }

    public static final String TEST_ENTITY_ID = "test";
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
            citation.addReview(getPopulatedReview(citation, ownerId));
        }
        return citation;
    }

    @NotNull
    protected Review getPopulatedReview(ChainCodeEntity entity) {
        return getPopulatedReview(entity, "test");
    }

    @NotNull
    protected Review getPopulatedReview(ChainCodeEntity entity, String ownerId) {
        Review review = new Review(entity, 0.9f, "Test review");
        review.setId(1l);
        review.setReviewedEntityId("test");
        review.setCreatorId("test");
        review.setOwnerId(ownerId);
        review.setCreatedTs(12345l);
        review.setUpdatedTs(12345l);
        return review;
    }
}