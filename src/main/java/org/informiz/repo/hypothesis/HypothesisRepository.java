package org.informiz.repo.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.model.SourceBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface HypothesisRepository extends CrudRepository<HypothesisBase, Long> {

    SourceBase findByEntityId(String entityId);

    SourceBase findById(long id);

}
