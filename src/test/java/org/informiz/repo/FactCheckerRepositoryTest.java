package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.model.FactCheckerBase;
import org.informiz.model.ModelTestUtils;
import org.informiz.model.Review;
import org.informiz.repo.checker.FactCheckerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class FactCheckerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FactCheckerRepository factCheckerRepo;

    FactCheckerBase chuck;

    Review review;


    @BeforeEach
    public void setup() {
        chuck = new FactCheckerBase("chuck", "chuck@informiz.org", "https://some.link");
        entityManager.persist(chuck);
        review = ModelTestUtils.getPopulatedReview(chuck, null);

        chuck.addReview(review);
        entityManager.persist(chuck);
        entityManager.flush();

    }

    private void validateChuck(FactCheckerBase found) {
        assertEquals(chuck.getName(), found.getName());
        assertEquals(chuck.getEntityId(), found.getEntityId());
        assertNotNull(found.getLocalId());
        assertEquals(1, found.getReviews().size());
        assertEquals(review.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindBy_thenReturnCheckerWithReviews() {
        // TODO: find-by name/email does not guarantee reviews, but test-repo actually returns same object (chuck)

        FactCheckerBase found = factCheckerRepo.findByName(chuck.getName());
        validateChuck(found);

        found = factCheckerRepo.findByEmail(chuck.getEmail());
        validateChuck(found);

        found = factCheckerRepo.findByEntityId(chuck.getEntityId());
        validateChuck(found);

        found = factCheckerRepo.findById(chuck.getLocalId()).get();
        validateChuck(found);
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnCheckersWithReviews() {
        Iterable<FactCheckerBase> checkers = factCheckerRepo.findAll();

        List<FactCheckerBase> asList =  StreamSupport.stream(checkers.spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(1, asList.size());
        validateChuck(asList.get(0));
    }

}