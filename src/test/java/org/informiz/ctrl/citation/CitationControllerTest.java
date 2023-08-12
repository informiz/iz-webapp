package org.informiz.ctrl.citation;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.conf.ThymeLeafConfig;
import org.informiz.ctrl.ErrorHandlingAdvice;
import org.informiz.model.CitationBase;
import org.informiz.model.Review;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.source.SourceRepository;
import org.jetbrains.annotations.NotNull;
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

import java.util.Optional;

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

    //Todo GETs: 1 - viewCitation
    //Todo GETs: 2 - addCitation
    //Todo GETs: 3 - getCitation
    //Todo GETs: 4 - getAllCitation

    //AddCitation (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenMemberViewsAddCitation_thenAllowed() throws Exception {


        mockMvc.perform(get("/citation/add")
                        .secure(true))
                .andExpect(status().isOk());
    }

    //AddCitation (Viewer Forbidden)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerViewsAddCitation_thenForbidden() throws Exception {


        mockMvc.perform(get("/citation/add")
                        .secure(true))
                .andExpect(status().isForbidden());
    }

    //AddCitation (Checker Forbidden)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerViewsAddCitation_thenForbidden() throws Exception {


        mockMvc.perform(get("/citation/add")
                        .secure(true))
                .andExpect(status().isForbidden());
    }



    //AddCitation
    // Exceeds 500
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenAddCitationTextExceeds_thenErrorMsg() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/add")
                        .secure(true).with(csrf())
                        .param("link", "http://server.com")
                        .param("text", RandomStringUtils.random(501))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("size must be between 0 and 500")));
    }

    //Add Citation
    // Invalid URL
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenCitationURLisInvalid_thenErrorMsg() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/add")
                        .secure(true).with(csrf())
                        .param("link", "invalid")
                        .param("text", RandomStringUtils.random(500))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please provide a link to the source of the citation")));
    }

    //Update Citation (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenValidUpdateCitation_thenSucceeds() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.findById(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/details/1")
                        .secure(true).with(csrf())
                        .param("ownerId", citation.get().getOwnerId())
                        .param("link", "http://server.com")
                        .param("text", RandomStringUtils.random(500))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/citation/details/1"));
    }

    //Update Citation (Invalid OwnerID)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenInvalidCitationOwnerId_thenErrorMsg() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.findById(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/details/1")
                        .secure(true).with(csrf())
                        .param("ownerId", "120276")
                        .param("link", "http://server.com")
                        .param("text", RandomStringUtils.random(500))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    //Update Citation (Invalid Link)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateCitationInvalidLink_thenErrorMsg() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.findById(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/details/1")
                        .secure(true).with(csrf())
                        .param("id", citation.get().getLocalId().toString())//Todo Validation group doesn't enforce Id
                        .param("ownerId", citation.get().getOwnerId())
                        .param("link", "Invalid")
                        .param("text", RandomStringUtils.random(500))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please provide a link to the source of the citation")));
    }

    //Update Citation (Exceeds 500)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenUpdateCitationTextExceeds_thenErrorMsg() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.loadByLocalId(1l)).willReturn(citation);


        mockMvc.perform(post("/citation/details/1")
                        .secure(true).with(csrf())
                        .param("ownerId", citation.get().getOwnerId())
                        .param("id", "1")//Todo: Id not enforced by validation group
                        .param("link", "http://server.com")
                        .param("text", RandomStringUtils.random(501))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("size must be between 0 and 500")));
    }

    //Delete Citation(No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenValidDeleteCitation_thenSucceeds() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.findById(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/delete/1")
                        .secure(true).with(csrf())
                        .param("ownerId", citation.get().getOwnerId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/citation/all"));
    }

    //Delete Citation(Not owner)
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    void whenDeleteCitationNotOwner_thenForbidden() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        //citation.get().setOwnerId(DEFAULT_TEST_CHECKER_ID);
        given(repo.findById(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/delete/1")
                        .secure(true).with(csrf())
                        .param("ownerId", citation.get().getOwnerId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }


    //Add a valid Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddValidReviewToCitation_thenSucceeds() throws Exception {

        Optional<CitationBase> citation = Optional.of(getPopulatedCitation());
        given(repo.loadByLocalId(1l)).willReturn(citation);

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .param("reviewedEntityId", citation.get().getEntityId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }

    //Add Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenAddCitationReviewNoAuth_thenForbidden() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }


    //Add Review (OwnerId)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenNotOwnerAddReviewOfCitation_thenForbidden() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("reviewedEntityId", review.getReviewedEntityId())
                        .param("rating", "0.82")
                        .param("comment", "Changed Comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }


    //Add Review No Rating
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenAddReviewToCitationNoRating_thenErrorMsg() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please submit rating between 0.0 and 1.0")));
    }

    //Add Review Comment Exceeds
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenReviewCommentExceeds_thenErrorMsg() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .param("comment", RandomStringUtils.random(256))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Comment must be under 255 characters")));
    }

    //Edit Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditValidReviewToCitation_thenSucceeds() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("id", review.getId().toString())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("reviewedEntityId", review.getReviewedEntityId())
                        .param("comment", RandomStringUtils.random(255))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }


    //Edit Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerEditsReviewOfCitation_thenForbidden() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    //Edit Review (OwnerId)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenNotOwnerIdToEditCitationReview_thenForbidden() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        //review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("reviewedEntityId", review.getReviewedEntityId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    //Edit Review No Rating
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewNoRating_thenErrorMsg() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please submit rating between 0.0 and 1.0")));
    }

    //Edit Review Invalid ReviewedEntityId
    @Test
    @Disabled("Currently no error message for reviewedEntityId")
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewBlankReviewedEntityId_thenErrorMsg() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("id", review.getId().toString())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("reviewedEntityId", "")
                        .param("comment", RandomStringUtils.random(255))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("No Error MSG")));
    }

    //Todo Edit Review reviewedEntityId Exceeds?



    //Edit Review Comment Exceeds 255
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenEditReviewCommentExceeds255_thenErrorMsg() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("comment", RandomStringUtils.random(256))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Comment must be under 255 characters")));
    }

    //Delete Review (No Errors)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenDeletesReviewOfCitation_succeedIfOwner() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        mockMvc.perform(post("/citation/1/review/del/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("reviewedEntityId", review.getReviewedEntityId())
                        .param("id", review.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }

    //Delete Review (No Auth)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerDeletesReviewOfCitation_thenForbidden() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/del/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("id", review.getId().toString())
                        .param("reviewedEntityId", review.getReviewedEntityId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }



    //Delete Review (No Id)
    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenDeletesCitationReviewNoId_thenErrorMessage() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        mockMvc.perform(post("/citation/1/review/del/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please provide an ID")));
    }





    @NotNull
    private static CitationBase getPopulatedCitation() {
        CitationBase citation = new CitationBase();
        citation.setLocalId(1l);
        citation.setEntityId("test");
        citation.setCreatorId("test");
        citation.setOwnerId("test");
        citation.setCreatedTs(12345l);
        citation.setUpdatedTs(12345l);
        citation.setLink("https://informiz.org");
        citation.setText("Test citation");
        return citation;
    }

    @NotNull
    private static Review getPopulatedReview(CitationBase citation) {
        Review review = new Review(citation, 0.9f, "Test review");
        review.setId(1l);
        review.setReviewedEntityId("test");
        review.setCreatorId("test");
        review.setOwnerId("test");
        review.setCreatedTs(12345l);
        review.setUpdatedTs(12345l);
        return review;
    }
}