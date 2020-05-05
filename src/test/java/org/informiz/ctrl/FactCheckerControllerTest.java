package org.informiz.ctrl;

import mockit.Mock;
import mockit.MockUp;
import org.informiz.ctrl.checker.CheckerCCDao;
import org.informiz.model.FactCheckerBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FactCheckerControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;


/*
    // TODO: can I access the db from the test?
    @Autowired
    private TestEntityManager entityManager;
*/

    FactCheckerBase chuck;
    FactCheckerBase cary;

    @BeforeAll
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        chuck.setEntityId("testEntityID");
        cary = new FactCheckerBase("cary", "cary@informiz.org", "https://other.link");
        cary.setEntityId("anotherTestEntityID");
/*
        entityManager.persist(chuck);
        entityManager.flush();
*/
    }


    @Test
    void whenAddChecker_thenReturnsUserResource() throws Exception {
        new MockUp<CheckerCCDao>() {
            @Mock
            public FactCheckerBase addFactChecker(HttpSession session, FactCheckerBase checker) {
                return cary;
            }
        };

        MvcResult mvcResult = mockMvc.perform(post("/factchecker/add")
                .param("name", cary.getName())
                .param("email", cary.getEmail())
                .param("link", cary.getLink())
                .param("entityId", cary.getEntityId())
                .param("score.reliability", cary.getScore().getReliability().toString())
                .param("score.confidence", cary.getScore().getConfidence().toString())
                .contentType("application/json"))
                .andReturn();

        String result = mvcResult.getResponse().getContentAsString();


    }
}