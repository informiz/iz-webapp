package org.informiz.repo;

import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.informiz.WithCustomAuth;
import org.informiz.model.InformiBase;
import org.informiz.model.ModelTestUtils;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.informi.InformiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.informiz.model.ModelTestUtils.getPopulatedReference;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=debug", "hibernate.cache.use_second_level_cache=false"})
public class InformiRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InformiRepository informiRepo;

    InformiBase informi;
    Reference ref;
    Review rev;

    Statistics statistics;

    PersistenceUnitUtil unitUtil;

    @BeforeEach
    public void setup() {
        // Prepare an informi with a review and a reference
        informi = getPopulatedInformi(null);
        rev = ModelTestUtils.getPopulatedReview(informi, null);
        ref = getPopulatedReference(informi, "TestReference", null);
        informi.addReview(rev);
        informi.addReference(ref);
        // Save to DB
        entityManager.persistAndFlush(informi);

        // Objects providing access to underlying DB interaction (queries performed, properties loaded)
        unitUtil = entityManager.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        statistics = entityManager.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class)
                .getStatistics();

        // Reset before test
        entityManager.clear();
        statistics.clear();
    }

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByName_thenReturnPersistedInformiFullData() {
        InformiBase found = informiRepo.findByName(informi.getName());

        verifyLoading(found, 1, new String[] {"reviews", "references"}, new String[] {"sources"});
        verifyInformi(found);
    }


    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnPersistedInformiPreviewData() {
        List<InformiBase> all = StreamSupport
                .stream(informiRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        InformiBase found = all.get(0);

        verifyLoading(found, 1, new String[] {"reviews"}, new String[] {"references", "sources"});
        verifyInformi(found);
    }

    private void verifyInformi(InformiBase found) {
        assertEquals(informi.getName(), found.getName());
        assertEquals(1, found.getReviews().size());
        assertEquals(rev.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }

    private void verifyLoading(InformiBase found, int expectedQueryCount, String[] loaded, String[] unLoaded) {
        assertEquals(expectedQueryCount, statistics.getQueryExecutionCount());
        for (String prop: loaded)
            assertTrue(unitUtil.isLoaded(found, prop), String.format("%s not loaded", prop));

        for (String prop: unLoaded)
            assertFalse(unitUtil.isLoaded(found, prop), String.format("%s is loaded", prop));

        // TODO: can also verify cascading on update/delete with getEntity[Update/Delete]Count
    }
}