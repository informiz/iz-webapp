package org.informiz.repo.reference;

import org.informiz.model.ReferenceTextBase;
import org.informiz.model.SourceBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface ReferenceRepository extends CrudRepository<ReferenceTextBase, Long> {

    SourceBase findByEntityId(String entityId);

    SourceBase findById(long id);

}
