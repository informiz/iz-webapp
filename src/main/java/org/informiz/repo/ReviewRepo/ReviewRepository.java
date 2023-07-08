package org.informiz.repo.ReviewRepo;

import org.informiz.model.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// TODO: is this needed?
public interface ReviewRepository extends CrudRepository<Review, Long> {

    Review findById(long id);

    Review findByCreatorIdAndReviewedEntityId(String creatorId, @Param("reviewed_entity_id") String reviewed);

    List<Review> findByReviewedEntityId(@Param("reviewed_entity_id") String reviewed);

    List<Review> findByCreatorId(String creatorId);

    long deleteByCreatorIdAndReviewedEntityId(String creatorId, @Param("reviewed_entity_id") String reviewed);
}
