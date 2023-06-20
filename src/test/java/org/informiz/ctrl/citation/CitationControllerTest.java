package org.informiz.ctrl.citation;

import org.hamcrest.core.StringContains;
import org.informiz.WithCustomAuth;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.model.CitationBase;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.source.SourceRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_CHECKER;
import static org.informiz.auth.InformizGrantedAuthority.ROLE_VIEWER;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, CitationController.class})
@ActiveProfiles("test")
@WebMvcTest(CitationController.class)
class CitationControllerTest {

    @MockBean
    private CitationRepository repo;

    @MockBean
    private SourceRepository sourceRepo;

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
                        .contentType("application/json"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/citation/details/1"));
    }

    @Test
    @WithCustomAuth(role = {ROLE_CHECKER})
    void whenCheckerReviewsCitationNoRating_thenError() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("must not be null")));
    }


    @Test
    @WithCustomAuth(role = {ROLE_VIEWER})
    void whenViewerReviewsCitation_thenForbidden() throws Exception {

        given(repo.loadByLocalId(1l)).willReturn(Optional.of(getPopulatedCitation()));

        mockMvc.perform(post("/citation/1/review/")
                        .secure(true).with(csrf())
                        .param("rating", "0.82")
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
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

    // TODO: complete tests
    @Test
    void editReview() {
    }

    @Test
    void deleteReview() {
    }
}