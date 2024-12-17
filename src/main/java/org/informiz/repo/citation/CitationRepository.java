package org.informiz.repo.citation;

import org.informiz.model.ChainCodeEntity;
import org.informiz.model.CitationBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

//    Adding a quarry to retreive a list of the last 5 citations(in citations repo)
//    @Query("SELECT c FROM CitationBase c ORDER BY c.lastUpdated DESC")
//    List<CitationBase> findTop5ByOrderByLastUpdatedDesc();

//    @Query("SELECT c FROM CitationBase c ORDER BY c.updatedTs DESC")
//    List<CitationBase> findTop5ByOrderByUpdatedTsDesc();

    @Query("SELECT c FROM CitationBase c ORDER BY c.updatedTs DESC")
    List<CitationBase> findTop5ByOrderByUpdatedTsDesc(Pageable pageable);

    @Query("SELECT DISTINCT c FROM CitationBase c " +
            "LEFT JOIN FETCH c.reviews " +
            "LEFT JOIN FETCH c.sources " +
            "LEFT JOIN FETCH c.references " +
            "ORDER BY c.updatedTs DESC")
    List<CitationBase> findTop5ByOrderByUpdatedTsDescWithRelations(Pageable pageable);


      /*
      @Query("SELECT c FROM CitationBase c WHERE c.updatedTs > :lastUpdatedTs ORDER BY c.updatedTs ASC")
      List<CitationBase> findByUpdatedTsGreaterThanOrderByUpdatedTsAsc(@Param("lastUpdatedTs") Long UpdatedTs);
      */

    @Query("SELECT DISTINCT c FROM CitationBase c " +
            "LEFT JOIN FETCH c.reviews " +
            "LEFT JOIN FETCH c.sources " +
            "LEFT JOIN FETCH c.references " +
            "WHERE c.updatedTs > :lastUpdatedTs " +
            "ORDER BY c.updatedTs ASC")
    List<CitationBase> findByUpdatedTsGreaterThanOrderByUpdatedTsAsc(@Param("lastUpdatedTs") Long updatedTs);





}
