package org.informiz.repo.source;

import org.informiz.model.SourceBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static org.informiz.model.SourceBase.SOURCE_DATA;

public interface SourceRepository extends ChaincodeEntityRepo<SourceBase> {

    @EntityGraph(value = SOURCE_DATA)
    SourceBase findByName(String name);

    @Override
    @EntityGraph(value = SOURCE_DATA)
    SourceBase findByEntityId(String entityId);

    @Override
    @EntityGraph(value = SOURCE_DATA)
    Optional<SourceBase> findById(Long id);

    @Override
    @EntityGraph(value = SOURCE_DATA)
    Iterable<SourceBase> findAll();
}
