package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.model.InformiBase;
import org.informiz.model.ModelTestUtils;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.informi.InformiRepository;
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
import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.informiz.model.ModelTestUtils.getPopulatedReference;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class InformiRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;


    @Autowired
    private InformiRepository informiRepo;

    InformiBase informi;
    Reference ref;
    Review rev;

    @BeforeEach
    public void setup() {
        informi = getPopulatedInformi(null);
        rev = ModelTestUtils.getPopulatedReview(informi, null);
        ref = getPopulatedReference(informi, "TestReference", null);
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnPersistedInformi() {
        entityManager.persist(informi);
        entityManager.flush();

        informi.addReview(rev);
        informi.getReferences().add(ref);
        entityManager.persist(informi);
        entityManager.flush();

        List<InformiBase> all = StreamSupport
                .stream(informiRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        InformiBase found = all.get(0);
        assertEquals(informi.getName(), found.getName());

        assertEquals(1, found.getReviews().size());
        assertEquals(rev.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }
}