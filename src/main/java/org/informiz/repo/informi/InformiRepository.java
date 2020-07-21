package org.informiz.repo.informi;

import org.informiz.model.InformiBase;
import org.informiz.model.SourceBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface InformiRepository extends CrudRepository<InformiBase, Long> {

    SourceBase findByEntityId(String entityId);

    SourceBase findById(long id);

    SourceBase findByName(String name);
}
