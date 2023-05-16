package org.informiz.repo.citation;

import org.informiz.model.CitationBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static org.informiz.model.CitationBase.CITATION_DATA;
import static org.informiz.model.CitationBase.CITATION_PREVIEW;

public interface CitationRepository extends ChaincodeEntityRepo<CitationBase> {

    @Override
    @EntityGraph(value = CITATION_DATA)
    Optional<CitationBase> findById(Long id);

    @Override
    @EntityGraph(value = CITATION_PREVIEW)
    Iterable<CitationBase> findAll();

    @Override
    @EntityGraph(value = CITATION_DATA)
    CitationBase findByEntityId(String entityId);
}
