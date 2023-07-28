package org.informiz.ctrl.citation;

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
import static org.informiz.auth.InformizGrantedAuthority.ROLE_CHECKER;
import static org.informiz.auth.InformizGrantedAuthority.ROLE_VIEWER;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerReviewsCitation_thenSucceeds() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerReviewsCitationNoRating_thenErrorMsg() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Please submit rating between 0.0 and 1.0")));
    }


    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerReviewsCitation_thenForbidden() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }


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

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerEditsReviewOfCitation_succeedIfOwner() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        // Initially review owner-id not the same as authenticated user
        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("comment", "Changed Comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());

        // Use same owner-id for review as authenticated user
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        mockMvc.perform(post("/citation/1/review/edit/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("rating", "0.82")
                        .param("comment", "Changed Comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerDeletesReviewOfCitationNoId_thenErrorMessage() throws Exception {

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



    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerDeletesReviewOfCitation_succeedIfOwner() throws Exception {

        CitationBase citation = getPopulatedCitation();
        Review review = getPopulatedReview(citation);
        citation.addReview(review);
        given(repo.loadByLocalId(1l)).willReturn(Optional.of(citation));

        // Initially review owner-id not the same as authenticated user
        mockMvc.perform(post("/citation/1/review/del/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("id", review.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Use same owner-id for review as authenticated user
        review.setOwnerId(DEFAULT_TEST_CHECKER_ID);
        mockMvc.perform(post("/citation/1/review/del/")
                        .secure(true).with(csrf())
                        .param("ownerId", review.getOwnerId())
                        .param("id", review.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
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