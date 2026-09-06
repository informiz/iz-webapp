package org.informiz.ctrl.InformiController;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ControllerTest;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.ctrl.informi.InformiController;
import org.informiz.model.InformiBase;
import org.informiz.model.Reference;
import org.informiz.repo.informi.InformiRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
class InformiControllerTest extends ControllerTest<InformiBase> {
    public static final String ALL_INFORMI_TITLE = "Graphical snippets of information, ranked for reliability";
    public static final String NEW_INFORMI = "New Informi";
    public static final String UPDATE_INFORMI = "Update Informi";
    public static final String DETAILS = "Details";
    public static final String TEXT_EXCEEDS_MSG = "Description exceeds limit";
    public static final String INVALID_LINK = "A valid link to a media file is mandatory";
    public static final String COMMENT_SIZE = "Comment must be under 255 characters";
    @MockitoBean
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
    @Disabled("Requires authenticating to storage service")
    void whenMemberAddInformi_thenSucceeds() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("JGimage001.jpeg");
        File file = new File(url.getPath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "JGimage001.jpeg",
                "image/jpeg",
                 new FileInputStream(file).readAllBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format(URI_TEMPLATE, prefix(), "add"))
                        .file(mockFile)
                        .param("name", new String[]{RandomStringUtils.random(50)})
                        .param("description", new String[]{RandomStringUtils.random(1400)})
                        .secure(true).with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isFound()) // Expecting HTTP status OK (200)
                .andExpect(redirectedUrl(allEntitiesUrl()));
    }

    //Todo: Code duplication in the next four tests
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

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format(URI_TEMPLATE, prefix(), "add"))
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

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format(URI_TEMPLATE, prefix(), "add"))
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

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format(URI_TEMPLATE, prefix(), "add"))
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

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format(URI_TEMPLATE, prefix(), "add"))
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
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberDeleteInformi_thenSucceeds() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isFound(), redirectedUrl(allEntitiesUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER}, checkerId = "some member")
    void whenNotOwnerDeleteInformi_thenForbidden() throws Exception {

        verifyPostApiCall(getPopulatedEntity(DEFAULT_TEST_CHECKER_ID, null), "delete/1",  Map.of(
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID}),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenValidAddReference_thenSucceeds() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
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
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
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
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{""},
                        "refEntityId", new String[]{("test1")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceFactCheckedEntityIdExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{RandomStringUtils.random(256)},
                        "refEntityId", new String[]{"test1"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceBlankRefEntityId_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be blank"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceRefEntityIdExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceNullEntailment_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "entailment", new String[]{null}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be null"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceDegreeGreaterThanOne_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{"1.9f"}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must be less than or equal to 1.0"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceNullDegree_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{null}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("must not be null"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReferenceCommentExceeds_thenErrorMsg() throws Exception {
        verifyPostApiCall(getPopulatedEntity("some owner", null), "/reference/1",  Map.of(
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "comment", new String[]{RandomStringUtils.random(256)}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("size must be between 0 and 255"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerEditValidReference_thenSucceeds() throws Exception {
        InformiBase hinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference refi = getPopulatedReference(hinformi);
        refi.setDegree(0.72f);
        hinformi.addReference(refi);
        verifyPostApiCall(hinformi, "/reference/1/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")},
                        "degree", new String[]{"0.69f"},
                        "entailment", new String[]{"SUPPORTS"}),
                Arrays.asList(status().isFound(), redirectedUrl(updateEntityUrl())));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId="Some_Checker")
    void whenNotOwnerEditReference_thenForbidden() throws Exception {
        InformiBase hinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference refi = getPopulatedReference(hinformi);
        hinformi.addReference(refi);
        verifyPostApiCall(hinformi, "/reference/1/edit/",  Map.of(
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
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditReference_thenForbidden() throws Exception {
        InformiBase hinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference refi = getPopulatedReference(hinformi);
        hinformi.addReference(refi);
        verifyPostApiCall(hinformi, "/reference/1/edit/",  Map.of(
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
        InformiBase hinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference refi = getPopulatedReference(hinformi);
        hinformi.addReference(refi);
        verifyPostApiCall(hinformi, "/reference/1/edit/",  Map.of(
                        "id", new String[]{"1"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("")}),
                Arrays.asList(status().isOk(),
                        content().string(new StringContains("Test_Entity_Id_Of_Reasonable_Length"))));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReferenceCommentExceeds_thenErrorMsg() throws Exception {
        InformiBase mokinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference mokref = getPopulatedReference(mokinformi);
        mokinformi.addReference(mokref);
        verifyPostApiCall(mokinformi, "/reference/1/edit/",  Map.of(
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
    void whenEditReferenceNullDegree_thenErrorMsg() throws Exception {
        InformiBase mokinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference mokref = getPopulatedReference(mokinformi);
        mokinformi.addReference(mokref);
        verifyPostApiCall(mokinformi, "/reference/1/edit/",  Map.of(
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
    void whenValidDeletesReference_thenSucceed() throws Exception {

        InformiBase mokinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference mokref = getPopulatedReference(mokinformi);
        mokinformi.addReference(mokref);
        verifyPostApiCall(mokinformi, "/reference/1/ref/del/",  Map.of(
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

        InformiBase mokinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference mokref = getPopulatedReference(mokinformi);
        mokinformi.addReference(mokref);
        verifyPostApiCall(mokinformi, "/reference/1/ref/del/",  Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}
                ),
                Arrays.asList(status().isForbidden()));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER}, checkerId="Some_Checker")
    void whenNotOwnerDeletesReference_thenForbidden() throws Exception {

        InformiBase mokinformi = getPopulatedEntity("some ownewr", DEFAULT_TEST_CHECKER_ID);
        Reference mokref = getPopulatedReference(mokinformi);
        mokinformi.addReference(mokref);
        verifyPostApiCall(mokinformi, "/reference/1/ref/del/",  Map.of(
                        "id", new String[]{"6969"},
                        "ownerId", new String[]{DEFAULT_TEST_CHECKER_ID},
                        "factCheckedEntityId", new String[]{TEST_ENTITY_ID},
                        "refEntityId", new String[]{("test1")}
                ),
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

    @NotNull
    protected Reference getPopulatedReference (InformiBase populatedEntity) {
        Reference ref = new Reference();
        ref.setId(1l);
        ref.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        ref.setCreatorId(DEFAULT_TEST_CHECKER_ID);
        ref.setFactCheckedEntityId(TEST_ENTITY_ID);
        ref.setRefEntityId("entityId_of_some_citation");
        ref.setEntailment(Reference.Entailment.SUPPORTS);
        ref.setDegree(0.69f);
        ref.setComment("Ammona make it!");

        return ref;
    }


}