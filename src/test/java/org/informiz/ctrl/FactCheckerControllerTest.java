package org.informiz.ctrl;


import mockit.Mock;
import mockit.MockUp;
import org.informiz.ctrl.checker.CheckerCCDao;
import org.informiz.model.FactCheckerBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class FactCheckerControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;


    FactCheckerBase chuck;
    FactCheckerBase cary;

    @Before
    public void setup() {
        chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        chuck.setEntityId("testEntityID");
        cary = new FactCheckerBase("cary", "cary@informiz.org", "https://other.link");
        cary.setEntityId("anotherTestEntityID");
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    @WithMockUser(roles="ADMIN")
    public void whenAddChecker_thenReturnsAll() throws Exception {
        // Mock the chaincode DAO
        new MockUp<CheckerCCDao>() {
            @Mock
            public FactCheckerBase addFactChecker(HttpSession session, FactCheckerBase checker) {
                return cary;
            }
        };

        // No fact-checkers yet
        mockMvc.perform(get("/factchecker/all").secure(true))
                //.andExpect(status().isFound()).andExpect(redirectedUrl("/blah"))
                .andExpect(model().attribute("checkers", new ArrayList<FactCheckerBase>()));

        mockMvc.perform(post("/factchecker/add")
                .param("name", cary.getName())
                .param("email", cary.getEmail())
                .param("link", cary.getLink())
                .param("entityId", cary.getEntityId())
                .param("score.reliability", cary.getScore().getReliability().toString())
                .param("score.confidence", cary.getScore().getConfidence().toString())
                .contentType("application/json"))
                .andExpect(status().isFound())
                .andReturn();

/*
        // TODO: test actually adding the fact-checker?
        // Verify that all fact checkers are { cary }
        mockMvc.perform(get("/factchecker/all").secure(true))
                .andExpect(status().isOk())
                .andExpect(model().attribute("checkers", Arrays.asList(cary)));
*/
    }
}