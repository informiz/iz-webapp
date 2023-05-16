package org.informiz.repo.entity;

import org.informiz.model.ChainCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

// TODO: extend PagingAndSortingRepository instead?
@NoRepositoryBean
public interface ChaincodeEntityRepo<E extends ChainCodeEntity> extends CrudRepository<E, Long> {

    // TODO: is this necessary?
    E findByEntityId(String entityId);

    Optional<E> findById(Long localId);

    /**
     * A helper function for loading entities with lazy-fetch collections (e.g. reviews).
     * Override this method (or the findById method) in order to explicitly use an entity-graph for loading
     * @param localId the id in local-storage
     * @return An optional result, fully loaded if exists
     */
    default Optional<E> loadByLocalId(Long localId) {
        return findById(localId);
    }

}
