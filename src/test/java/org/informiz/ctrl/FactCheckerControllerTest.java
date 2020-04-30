package org.informiz.ctrl;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.FactCheckerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
//@SpringBootTest
@DataJpaTest
class FactCheckerControllerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FactCheckerRepository factCheckerRepo;


    @Test
    public void whenFindBy_thenReturnChecker() {
        FactCheckerBase chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        entityManager.persist(chuck);
        entityManager.flush();

        FactCheckerBase found = factCheckerRepo.findByName(chuck.getName());
        assertEquals(chuck.getName(), found.getName());

        found = factCheckerRepo.findByEmail(chuck.getEmail());
        assertEquals(chuck.getName(), found.getName());
    }
}