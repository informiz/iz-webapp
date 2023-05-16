package org.informiz.repo.informi;

import org.informiz.model.InformiBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static org.informiz.model.InformiBase.INFORMI_DATA;
import static org.informiz.model.InformiBase.INFORMI_PREVIEW;

public interface InformiRepository extends ChaincodeEntityRepo<InformiBase> {

    @Override
    @EntityGraph(value = INFORMI_DATA)
    Optional<InformiBase> findById(Long id);

    @EntityGraph(value = INFORMI_DATA)
    InformiBase findByName(String name); // TODO: remove?

    @Override
    @EntityGraph(value = INFORMI_PREVIEW)
    Iterable<InformiBase> findAll();

    @Override
    @EntityGraph(value = INFORMI_DATA)
    InformiBase findByEntityId(String entityId);
}
