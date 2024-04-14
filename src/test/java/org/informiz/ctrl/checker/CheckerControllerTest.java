package org.informiz.ctrl.checker;

import org.informiz.WithCustomAuth;
import org.informiz.auth.AuthUtils;
import org.informiz.auth.InformizGrantedAuthority;
import org.informiz.conf.MethodSecurityConfig;
import org.informiz.conf.SecurityConfig;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.Arrays;
import java.util.List;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_ADMIN;
import static org.informiz.auth.InformizGrantedAuthority.ROLE_VIEWER;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, MethodSecurityConfig.class, FactCheckerController.class})
@ActiveProfiles("test")
@WebMvcTest(FactCheckerController.class)
public class CheckerControllerTest {

    @MockBean
    private FactCheckerRepository repo;

    @MockBean
    private SecurityConfig.ClientIdService googleOAuthService;

    @Autowired
    private MockMvc mockMvc;


    static private FactCheckerBase chuck;
    static private FactCheckerBase cary;

    static private List<FactCheckerBase> allCheckers;


    @BeforeAll
    public static void setup() {
        chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        chuck.setEntityId("testEntityID");
        chuck.setLocalId(1l);
        cary = new FactCheckerBase("cary", "cary@informiz.org", "https://other.link");
        cary.setEntityId("anotherTestEntityID");
        cary.setLocalId(2l);
        allCheckers = Arrays.asList(chuck, cary);

    }


    @Test
    public void whenViewCheckers_thenReturnsAll() throws Exception {

        given(repo.findAll()).willReturn(allCheckers);

        mockMvc.perform(get("/factchecker/")
                        .with(oauth2Login().authorities(AuthUtils.anonymousAuthorities()))
                        .secure(true)
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("checkers", allCheckers));

    }

    @Test
    public void whenUnAuthAddChecker_thenForbid() throws Exception {


        mockMvc.perform(post("/factchecker/add")
                        .with(oauth2Login().authorities(AuthUtils.anonymousAuthorities()))
                        .secure(true).with(csrf())
                        .param("name", cary.getName())
                        .param("email", cary.getEmail())
                        .param("link", cary.getLink())
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/factchecker/add")
                        .with(oauth2Login().authorities(new InformizGrantedAuthority(ROLE_VIEWER, "entityId")))
                        .secure(true).with(csrf())
                        .param("name", cary.getName())
                        .param("email", cary.getEmail())
                        .param("link", cary.getLink())
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithCustomAuth(role = {ROLE_ADMIN})
    public void whenAdminAddChecker_thenAdd() throws Exception {

        mockMvc.perform(post("/factchecker/add")
                        .secure(true).with(csrf())
                        .param("name", cary.getName())
                        .param("email", cary.getEmail())
                        .param("link", cary.getLink())
                        .param("entityId", cary.getEntityId())
                        .param("score.reliability", cary.getScore().getReliability().toString())
                        .param("score.confidence", cary.getScore().getConfidence().toString())
                        .contentType("application/json"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/factchecker/all"));
    }
}