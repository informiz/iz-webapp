package org.informiz.repo.reference;

import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.springframework.data.repository.CrudRepository;

public interface ReferenceRepository extends CrudRepository<Reference, Long> {
    Reference findById(long id);
}
