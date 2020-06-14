package org.informiz.repo.source;

import org.informiz.model.SourceBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface SourceRepository extends CrudRepository<SourceBase, Long> {

    SourceBase findByEntityId(String entityId);

    SourceBase findById(long id);

    SourceBase findByName(String name);
}
