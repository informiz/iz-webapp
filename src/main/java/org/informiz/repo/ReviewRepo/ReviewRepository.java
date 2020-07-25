package org.informiz.repo.ReviewRepo;

import org.informiz.model.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// TODO: extend PagingAndSortingRepository instead?
public interface ReviewRepository extends CrudRepository<Review, Long> {

    Review findById(long id);

    Review findByCheckerAndReviewed(String checker, String reviewed);

    List<Review> findByReviewed(String reviewed);

    List<Review> findByChecker(String checker);

    long deleteByCheckerAndReviewed(@Param("checker") String checker, @Param("reviewed") String reviewed);
}
