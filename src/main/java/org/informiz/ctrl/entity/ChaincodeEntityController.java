package org.informiz.ctrl.entity;

import org.informiz.auth.AuthUtils;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.Review;
import org.informiz.repo.ReviewRepo.ReviewRepository;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;


public class ChaincodeEntityController<T extends ChainCodeEntity> {
    public static final String REVIEW_ATTR = "review";

    @Autowired
    protected ChaincodeEntityRepo<T> entityRepo;
    @Autowired
    private ReviewRepository reviewRepo;
    // TODO: chaincode DAO

    protected T reviewEntity(String entityId, Review review, Authentication authentication) {
        String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());
        review.setChecker(checker);
        review.setReviewed(entityId);

        // TODO: Pretty sure that's not the correct way to add/edit a review!!! Check this

        T current = entityRepo.findByEntityId(entityId);
        current.getReviews().remove(review);
        reviewRepo.deleteByCheckerAndReviewed(checker, entityId);
        reviewRepo.save(review);
        current.getReviews().add(review);
        entityRepo.save(current);
        return current;
    }

}
