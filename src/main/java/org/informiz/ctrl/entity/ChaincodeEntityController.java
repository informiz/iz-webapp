package org.informiz.ctrl.entity;

import org.informiz.auth.AuthUtils;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.Set;


public class ChaincodeEntityController<T extends ChainCodeEntity> {
    public static final String REVIEW_ATTR = "review";

    @Autowired
    protected ChaincodeEntityRepo<T> entityRepo;
    // TODO: chaincode DAO

    protected T reviewEntity(T entity, Review review, Authentication authentication) {
        String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());

        Review current = entity.getCheckerReview(checker);
        if (current != null) {
            current.setRating(review.getRating());
            current.setComment(review.getComment());
        } else {
            entity.addReview(new Review(checker, entity, review.getRating(), review.getComment()));
        }
        return entity;
    }

    protected <S extends ChainCodeEntity & EntityWithReferences> void referenceEntity(S entity, Reference reference,
                                                                                      Authentication authentication) {

        Set<Reference> references = entity.getReferences();
        String creator = AuthUtils.getUserEntityId(authentication.getAuthorities());
        reference.setCreatorId(creator);
        reference.setReviewed(entity);

        // TODO: confusing user-experience: editing someone else's reference will create a new one
        Reference current = references.stream().filter(ref ->
                ref.equals(reference)).findFirst().orElse(null);
        if (current != null) {
            current.setEntailment(reference.getEntailment());
            current.setDegree(reference.getDegree());
            current.setComment(reference.getComment());
        } else {
            references.add(new Reference(entity, reference));
        }
    }
}
