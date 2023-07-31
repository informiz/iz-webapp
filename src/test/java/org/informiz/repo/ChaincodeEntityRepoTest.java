package org.informiz.repo;

import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=debug", "hibernate.cache.use_second_level_cache=false"})
public abstract class ChaincodeEntityRepoTest <E extends ChainCodeEntity, T extends ChaincodeEntityRepo>{

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected T chaincodeEntityRepo;

    E chaincodeEntity;
    Reference ref;
    Review rev;

    Statistics statistics;

    PersistenceUnitUtil unitUtil;


    protected abstract void initEntities();

    @BeforeEach
    public void setup() {
        // Prepare an citation with a review and a reference
        initEntities();
        // Save to DB
        chaincodeEntity = entityManager.persistAndFlush(chaincodeEntity);

        // Objects providing access to underlying DB interaction (queries performed, properties loaded)
        unitUtil = entityManager.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        statistics = entityManager.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class)
                .getStatistics();

        // Reset before test
        entityManager.clear();
        statistics.clear();
    }



}