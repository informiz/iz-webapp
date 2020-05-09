package org.informiz.ctrl;

import mockit.Mock;
import mockit.MockUp;
import org.informiz.ctrl.checker.CheckerCCDao;
import org.informiz.model.FactCheckerBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest(FactCheckerController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FactCheckerControllerTest {

    @Autowired
    private MockMvc mockMvc;


    FactCheckerBase chuck;
    FactCheckerBase cary;

    @BeforeAll
    public void setup() {
        chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        chuck.setEntityId("testEntityID");
        cary = new FactCheckerBase("cary", "cary@informiz.org", "https://other.link");
        cary.setEntityId("anotherTestEntityID");
    }


    @Test
    @WithMockUser(roles="ADMIN")
    void whenAddChecker_thenReturnsAll() throws Exception {
        // Mock the chaincode DAO
        new MockUp<CheckerCCDao>() {
            @Mock
            public FactCheckerBase addFactChecker(HttpSession session, FactCheckerBase checker) {
                return cary;
            }
        };

        // No fact-checkers yet
        mockMvc.perform(get("/factchecker/all"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("checkers", new ArrayList<FactCheckerBase>()));

        // Add Cary
        mockMvc.perform(post("/factchecker/add")
                .param("name", cary.getName())
                .param("email", cary.getEmail())
                .param("link", cary.getLink())
                .param("entityId", cary.getEntityId())
                .param("score.reliability", cary.getScore().getReliability().toString())
                .param("score.confidence", cary.getScore().getConfidence().toString())
                .contentType("application/json"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/factchecker/all"))
                .andReturn();

        // Verify that all fact checkers are { cary }
        mockMvc.perform(get("/factchecker/all"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("checkers", Arrays.asList(cary)));
    }
}