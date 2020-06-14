package org.informiz.repo.checker;

import org.informiz.model.FactCheckerBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface FactCheckerRepository extends CrudRepository<FactCheckerBase, Long> {

    FactCheckerBase findByEntityId(String entityId);

    FactCheckerBase findById(long id);

    FactCheckerBase findByName(String name);

    FactCheckerBase findByEmail(String email);
}
