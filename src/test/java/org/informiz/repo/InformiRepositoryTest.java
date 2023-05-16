package org.informiz.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.informiz.WithCustomAuth;
import org.informiz.model.FactCheckerBase;
import org.informiz.model.InformiBase;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.checker.FactCheckerRepository;
import org.informiz.repo.informi.InformiRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InformiRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;


    @Autowired
    private InformiRepository informiRepo;

    InformiBase informi;
    Reference ref;
    Review rev;

    @BeforeAll
    public void setup() {
        informi = new InformiBase();
        informi.setName("Test Informi");
        informi.setDescription("Test description for Informi");
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByName_thenReturnInformi() {
        EntityManager em = entityManager.getEntityManager();
        PersistenceUnitUtil unitUtil =
                em.getEntityManagerFactory().getPersistenceUnitUtil();

        entityManager.persist(informi);
        entityManager.flush();

        rev = new Review(informi, 0.92f, "Test review");
        informi.getReviews().add(rev);

        ref = new Reference();
        ref.setFactChecked(informi);
        ref.setRefEntityId("TestReference");
        ref.setEntailment(Reference.Entailment.SUPPORTS);
        ref.setDegree(0.9f);
        informi.getReferences().add(ref);

        entityManager.persist(informi);
        entityManager.flush();
        em.getTransaction().commit();

        List<InformiBase> all = StreamSupport
                .stream(informiRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        InformiBase found = all.get(0);
        assertEquals(informi.getName(), found.getName());
        assertTrue(unitUtil.isLoaded(found, "reviews"));
    }
}