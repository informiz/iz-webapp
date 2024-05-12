package org.informiz.ctrl.InformiController;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.ctrl.informi.InformiController;
import org.junit.jupiter.api.Disabled;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.ctrl.citation.CitationController;
import org.informiz.model.CitationBase;
import org.informiz.model.InformiBase;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.informi.InformiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import static org.informiz.MockSecurityContextFactory.DEFAULT_TEST_CHECKER_ID;
import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InformiControllerTest.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, ThymeLeafConfig.class, InformiController.class, ErrorHandlingAdvice.class})

class InformiControllerTest extends org.informiz.ctrl.ControllerTest<InformiBase> {
    public static final String ALL_INFORMI_TITLE = "Graphical snippets of information, ranked for reliability";
    public static final String NEW_INFORMI = "New Informi";
    public static final String UPDATE_INFORMI = "Update Informi";
    public static final String DETAILS = "Details";
    public static final String TEXT_EXCEEDS_MSG = "Description exceeds limit";
    public static final String INVALID_LINK = "A valid link to a media file is mandatory";
    public static final String COMMENT_SIZE = "Comment must be under 255 characters";
    @MockBean
    InformiRepository informiRepository;

    @Override
    protected String prefix(){
        return "informi";
    }

    @Override
    protected String allEntitiesTitle() {
        return ALL_INFORMI_TITLE;
    }
    @Override
    protected String newEntityTitle() {
        return NEW_INFORMI;
    }
    @Override
    protected String updateEntityTitle() {
        return UPDATE_INFORMI;
    }
    @Override
    protected String viewEntityTitle() {
        return DETAILS;
    }
    @Override
    protected String textExceedsMsg() {
        return TEXT_EXCEEDS_MSG;
    }
    protected String commentExceedsMsg() {
        return COMMENT_SIZE;
    }
    protected String EntityIllegalArgumentTitle() {
        return "Illegal argument, an error was logged and will be addressed by a developer"; }
    protected String invalidInformiLinkMsg() {
        return INVALID_LINK;
    }


    @Override
    protected String entityReviewUrl() {
        return "/informi/details/1";
    }

    //Todo: disable log
    //Todo: Fix Error Msg
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsInformiInvalidId_thenErrorMsg() throws Exception {
        verifyGetApiCall("view/1",
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(EntityIllegalArgumentTitle()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberAddInformi_thenSucceeds() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                 new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/informi/add")
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("description", new String[]{RandomStringUtils.random(1400)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isFound()) // Expecting HTTP status OK (200)
                .andExpect(redirectedUrl(allEntitiesUrl()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerAddsInformi_thenForbidden() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/informi/add")
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("description", new String[]{RandomStringUtils.random(1400)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerAddInformi_thenForbidden() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/informi/add")
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("description", new String[]{RandomStringUtils.random(1400)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenInformiURLisInvalid_thenErrorMsg() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/informi/add")
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("mediaPath", new String[]{"Invalid"})
                        .param("description", new String[]{RandomStringUtils.random(1400)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()) // Expecting HTTP status OK (200)
                .andExpect(content().string(new StringContains(invalidInformiLinkMsg())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddInformiTextExceeds_thenErrorMsg() throws Exception {

        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/informi/add")
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("description", new String[]{RandomStringUtils.random(1501)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()) // Expecting HTTP status OK (200)
                .andExpect(content().string(new StringContains(textExceedsMsg())));
    }

    //Todo: Validation group doesn't include Id and OwnerId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenOwnerUpdateInformi_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "name", new String[]{"informiTeatName"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "mediaPath", new String[]{"http://server.com"},
                        "description", new String[]{RandomStringUtils.random(1500)}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerUpdateInformi_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "mediaPath", new String[]{"http://server.com"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "description", new String[]{RandomStringUtils.random(1500)}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateInformiInvalidLink_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "mediaPath", new String[]{"Invalid"},
                        "description", new String[]{RandomStringUtils.random(1500)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(invalidInformiLinkMsg()))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateInformiTextExceeds_thenErrorMsg() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "details/1",  Map.of(
                        "id", new String[]{"1"},
                        "name", new String[]{"informiTeatName"},
                        "entityId", new String[]{TEST_ENTITY_ID},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "mediaPath", new String[]{"http://server.com"},
                        "description", new String[]{RandomStringUtils.random(1501)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains(textExceedsMsg()))));
    }
    //Todo Edit Review reviewedEntityId Exceeds?

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteInformi_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }

    @Override
    @NotNull
    protected InformiBase getPopulatedEntity(String ownerId, String reviewOwnerId) {
        InformiBase informi = new InformiBase();
        informi.setLocalId(1l);
        informi.setEntityId(TEST_ENTITY_ID);
        informi.setCreatorId("test");
        informi.setName("informiTeatName");
        informi.setOwnerId(ownerId);
        informi.setCreatedTs(12345l);
        informi.setUpdatedTs(12345l);
        informi.setMediaPath("https://informiz.org");
        informi.setDescription("Test informi");


        if(reviewOwnerId != null) {
            informi.addReview(getPopulatedReview(informi, reviewOwnerId));
        }
        return informi;
    }


}