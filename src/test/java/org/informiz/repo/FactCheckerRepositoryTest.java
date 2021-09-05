package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.auth.InformizGrantedAuthority;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class FactCheckerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FactCheckerRepository factCheckerRepo;


    @Test
    @WithCustomAuth
    public void whenFindBy_thenReturnChecker() {
        FactCheckerBase chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        chuck.setEntityId("testEntityID");
        entityManager.persist(chuck);
        entityManager.flush();

        FactCheckerBase found = factCheckerRepo.findByName(chuck.getName());
        assertEquals(chuck.getName(), found.getName());

        found = factCheckerRepo.findByEmail(chuck.getEmail());
        assertEquals(chuck.getName(), found.getName());
    }
}