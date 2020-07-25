package org.informiz.repo.entity;

import org.informiz.model.ChainCodeEntity;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface ChaincodeEntityRepo<E extends ChainCodeEntity> extends CrudRepository<E, Long> {

    E findByEntityId(String entityId);

}
