package org.informiz.ctrl;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.SecurityConfig;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.Review;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.informiz.repo.source.SourceRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public abstract class ControllerTest<T extends ChainCodeEntity> {
    public static final String TEST_ENTITY_ID = "Test_Entity_Id_Of_Reasonable_Length";


    @Autowired
    protected ChaincodeEntityRepo<T> repo;
    @MockBean
    protected SourceRepository sourceRepo;
    @MockBean
    protected SecurityConfig.ClientIdService googleOAuthService;
    @Autowired
    protected MockMvc mockMvc;

    protected abstract String prefix();

    protected abstract String newEntityTitle();

    protected abstract String allEntitiesTitle();

    protected abstract String updateEntityTitle();

    protected abstract String viewEntityTitle();

    protected String EntityIllegalArgumentTitle() {
        return "Illegal argument, an error was logged and will be addressed by a developer";
    }
    protected abstract String textExceedsMsg();

    protected String commentExceedsMsg() {
        return "";
    }

    protected String updateEntityUrl() {
        return String.format("/%s/%s", prefix(), "details/1");
    }

    protected String deleteEntityUrl() {
        return String.format("/%s/%s",prefix(), "delete/1");
    }

    protected String allEntitiesUrl() {
        return String.format("/%s/%s",prefix(), "all");
    }

    protected  String entityReviewUrl() {
        return String.format("/%s/%s",prefix(), "1/review/");
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsAllEntity_thenAllowed() throws Exception {

        verifyGetApiCall("all",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(allEntitiesTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsAllEntities_thenAllowed() throws Exception {

        verifyGetApiCall("all",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(allEntitiesTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsAddEntityForm_thenAllowed() throws Exception {

        verifyGetApiCall("add",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(newEntityTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsAddEntityForm_thenForbidden() throws Exception {

        verifyGetApiCall("add",
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsUpdateEntity_thenSucceeds() throws Exception {

        verifyGetApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(updateEntityTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsEditEntity_thenForbidden() throws Exception {

        verifyGetApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsEntity_thenAllowed() throws Exception {

        verifyGetApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(viewEntityTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenValidDeleteEntity_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidAddReview_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", null), "1/review/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}),
                Arrays.asList(status().isFound(), redirectedUrl(entityReviewUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddReview_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", null), "1/review/",  Map.of(
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReviewNoRating_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", null), "1/review/",  Map.of(
                        // "rating", new String[]{"0.82"}
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please submit rating between 0.0 and 1.0"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenReviewCommentExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", null), "1/review/",  Map.of(
                        "rating", new String[]{"0.82"},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{RandomStringUtils.random(256)}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(commentExceedsMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidEditReview_thenSucceeds() throws Exception {
        //Todo: Validation group doesn't require ownerId, reviewedEntityId

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isFound()));
    }
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId="Some_Checker")
    void whenNotOwnerEditReview_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "rating", new String[]{("0.82")},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isForbidden()));
    }
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditsReview_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "rating", new String[]{("0.82")},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewNoRating_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "comment", new String[]{RandomStringUtils.random(255)}
                        //"rating", new String[]{("0.82")},
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please submit rating between 0.0 and 1.0"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewCommentExceeds255_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "rating", new String[]{("0.82")},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{(TEST_ENTITY_ID)},
                        "comment", new String[]{RandomStringUtils.random(256)}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Comment must be under 255 characters"))));
    }

    @Test
    @Disabled("Currently no error message for reviewedEntityId")
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewBlankReviewedEntityId_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/edit/",  Map.of(
                        "id", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "rating", new String[]{("0.82")},
                        "reviewedEntityId", new String[]{("")}
                ),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("No Error MSG"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenDeletesReviewOfEntity_thenSucceed() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID), "1/review/del/",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID}
                ),
                Arrays.asList(status().isFound(), redirectedUrl(entityReviewUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenDeleteEntityReviewNoId_thenErrorMessage() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID),"1/review/del/",  Map.of(
                        //"id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Please provide an ID"))));
    }
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId = "some checker")
    void whenNotOwnerDeletesReview_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity("some owner", DEFAULT_TEST_CHECKER_ID),"/1/review/del/",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "id", new String[]{"1"},
                        "reviewedEntityId", new String[]{TEST_ENTITY_ID}),

                Arrays.asList(status().isForbidden()));
    }

    protected void verifyGetApiCall(String path, List<ResultMatcher> matchers) throws Exception {
        performRequest(get(String.format("/%s/%s", prefix(), path)), Map.of(),
                matchers);
    }

    protected void verifyGetApiCall(T entity, String path, List<ResultMatcher> matchers) throws Exception {
        if (entity != null) {
            given(repo.loadByLocalId(1l)).willReturn(Optional.of(entity));
        }
        performRequest(get(String.format("/%s/%s", prefix(), path)), Map.of(),
                matchers);
    }

    protected void verifyPostApiCall(String path, Map<String, String[]> params, List<ResultMatcher> matchers) throws Exception {
        performRequest(post(String.format("/%s/%s", prefix(), path)), params,
                matchers);
    }

    protected void verifyPostApiCall(T entity, String path, Map<String, String[]> params, List<ResultMatcher> matchers) throws Exception {
        if (entity != null) {
            given(repo.loadByLocalId(1l)).willReturn(Optional.of(entity));
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
    protected abstract T getPopulatedEntity(String ownerId, String reviewOwnerId);

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
