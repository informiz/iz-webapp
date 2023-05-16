package org.informiz.repo.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static org.informiz.model.HypothesisBase.CLAIM_DATA;
import static org.informiz.model.HypothesisBase.CLAIM_PREVIEW;

public interface HypothesisRepository extends ChaincodeEntityRepo<HypothesisBase> {
    @Override
    @EntityGraph(value = CLAIM_DATA)
    Optional<HypothesisBase> findById(Long id);

    @Override
    @EntityGraph(value = CLAIM_PREVIEW)
    Iterable<HypothesisBase> findAll();

    @Override
    @EntityGraph(value = CLAIM_DATA)
    HypothesisBase findByEntityId(String entityId);
}
