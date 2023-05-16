package org.informiz.repo.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static org.informiz.model.FactCheckerBase.FACT_CHECKER_DATA;

// TODO: extend PagingAndSortingRepository instead?
public interface FactCheckerRepository extends ChaincodeEntityRepo<FactCheckerBase> {

    @Override
    @EntityGraph(value = FACT_CHECKER_DATA)
    Optional<FactCheckerBase> findById(Long id);

    @Override
    @EntityGraph(value = FACT_CHECKER_DATA)
    Iterable<FactCheckerBase> findAll();

    @EntityGraph(value = FACT_CHECKER_DATA)
    FactCheckerBase findByEntityId(String entityId);

    FactCheckerBase findByName(String name);

    FactCheckerBase findByEmail(String email);
}
