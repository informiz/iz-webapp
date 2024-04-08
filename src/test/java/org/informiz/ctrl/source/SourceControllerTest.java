package org.informiz.ctrl.source;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.model.SourceBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Map;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SourceController.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, SourceController.class, ErrorHandlingAdvice.class})

class SourceControllerTest extends org.informiz.ctrl.ControllerTest<SourceBase> {
    public static final String ALL_SOURCES_TITLE = "Known sources, e.g NASA or CNN, ranked for reliability";
    public static final String NEW_SOURCE = "New Source";
    public static final String TEST_TYPE = "BLOG";
    public static final String UPDATE_SOURCE = "Update Source";
    public static final String DETAILS = "Details";
    public static final String SIZE_MUST_BE_BETWEEN_0_AND_500 = "size must be between 0 and 500";
    public static final String INVALID_LINK = "Please provide a link to the source of the source";
    public static final String COMMENT_SIZE = "Comment must be under 255 characters";
    public static final String TYPE_MSG = "Type is mandatory";

    @Override
    protected String prefix(){
        return "source";
    }

    @Override
    protected String allEntitiesTitle() {
        return ALL_SOURCES_TITLE;
    }

    @Override
    protected String newEntityTitle() {
        return NEW_SOURCE;
    }
    @Override
    protected String updateEntityTitle() {
        return UPDATE_SOURCE;
    }
    //@Override
    @Override
    protected String viewEntityTitle() {
        return "Source Details";
    }
    @Override
    protected String textExceedsMsg() {
        return "Description must be under 500 characters";
    }
    protected String commentExceedsMsg() {
        return COMMENT_SIZE;
    }
    protected String nullTypeMsg() {
        return TYPE_MSG;
    }
    protected String EntityIllegalArgumentTitle() {return "Illegal argument, an error was logged and will be addressed by a developer"; }
    protected String invalidSourceLinkMsg() {
        return  "Please provide a valid link";
    }
    @Override
    protected String deleteEntityUrl() {
        return "/source/all";
    }
    @Override
    protected String entityReviewUrl() {
        return "/source/details/1";
    }

    //Todo: disable log
    //Todo crashes in the background
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsSourceInvalidId_thenErrorMsg() throws Exception {

        verifyGetApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(EntityIllegalArgumentTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberAddSource_thenSucceeds() throws Exception {

        verifyPostApiCall("/add",  Map.of(

                "srcType", new String[]{TEST_TYPE},
                        "name", new String[]{TEST_ENTITY_ID},
                        "link", new String[]{"http://server.com"},
                        "description", new String[]{RandomStringUtils.random(500)},
                        "reliability", new String[]{"0.9"},
                        "confidence", new String[]{"0.5"}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsSource_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "srcType", new String[]{TEST_TYPE},
                        "link", new String[]{"http://server.com"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(500)},
                        "reliability", new String[]{"0.9"},
                        "confidence", new String[]{"0.5"}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddSource_thenForbidden() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "srcType", new String[]{TEST_TYPE},
                        "link", new String[]{"http://server.com"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    //Todo: Validation group doesn't include Id and OwnerId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenOwnerUpdateSource_thenSucceeds() throws Exception {

        SourceBase populatedEntity = getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null);
        populatedEntity.setDescription(RandomStringUtils.random(500));
        verifyPostApiCall(populatedEntity, "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "srcType", new String[]{TEST_TYPE},
                        "name", new String[]{TEST_ENTITY_ID},
                        "link", new String[]{"http://server.com"},
                        "description", new String[]{RandomStringUtils.random(500)},
                        "reliability", new String[]{"0.9"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateSourceNoType_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "srcType", new String[]{null},
                        "link", new String[]{"http://server.com"},
                        "name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(nullTypeMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenSourceURLisInvalid_thenErrorMsg() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "srcType", new String[]{TEST_TYPE},
                        "link", new String[]{"Invalid"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(invalidSourceLinkMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddSourceDescriptionExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall("add",  Map.of(
                        "type", new String[]{TEST_TYPE},
                        "link", new String[]{"http://server.com"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerUpdateSource_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "type", new String[]{TEST_TYPE},
                        "link", new String[]{"http://server.com"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateSourceInvalidLink_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "srcType", new String[]{"TEST_TYPE"},
                        "link", new String[]{"Invalid"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(500)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(invalidSourceLinkMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateSourceDescriptionExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "srcType", new String[]{TEST_TYPE},
                        "link", new String[]{"http://server.com"},
                        "Name", new String[]{TEST_ENTITY_ID},
                        "description", new String[]{RandomStringUtils.random(501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteSource_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }

    @Override
    @NotNull
    protected SourceBase getPopulatedEntity(String ownerId, String reviewOwnerId) {
        SourceBase source = new SourceBase();
        source.setLocalId(1l);
        source.setEntityId(TEST_ENTITY_ID);
        source.setCreatorId("test");
        source.setOwnerId(ownerId);
        source.setCreatedTs(12345l);
        source.setUpdatedTs(12345l);
        source.setLink("https://informiz.org");
        source.setName("Test Source Name");
        source.setSrcType(SourceBase.SourceType.NEWS);
        source.setDescription("Some Meaningful Text");

        if(reviewOwnerId != null) {
            source.addReview(getPopulatedReview(source, reviewOwnerId));
        }
        return source;
    }
}