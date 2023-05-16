package org.informiz.ctrl.entity;

import org.informiz.auth.AuthUtils;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.FactCheckedEntity;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;

import java.util.Set;


public abstract class ChaincodeEntityController<T extends ChainCodeEntity> {
    public static final String REVIEW_ATTR = "review";

    protected final ChaincodeEntityRepo<T> entityRepo;
    // TODO: chaincode DAO


    public ChaincodeEntityController(ChaincodeEntityRepo<T> entityRepo) {
        this.entityRepo = entityRepo;
    }

    protected T reviewEntity(Long id, Review review, Authentication authentication, BindingResult result) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));

        if (! result.hasFieldErrors("rating")) {
            String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());

            Review current = entity.getCheckerReview(checker);
            if (current != null) {
                current.setRating(review.getRating());
                current.setComment(review.getComment());
            } else {
                entity.addReview(new Review(entity, review.getRating(), review.getComment()));
            }
        }
        return entity;
    }

    protected void deleteReview(long id, Long revId, Authentication authentication) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));
        String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());
        Review current = entity.getCheckerReview(checker);
        if (current != null && current.getId().equals(revId)) {
            entity.removeReview(current);
        } // TODO: warn if no review or different id
    }


    // TODO: standardize handling of review/reference/sourcing of entities (directly on entity? Concurrency issues?)
    protected <S extends FactCheckedEntity> void referenceEntity(S entity, Reference reference,
                                                                 Authentication authentication) {

        Set<Reference> references = entity.getReferences();
        String creator = AuthUtils.getUserEntityId(authentication.getAuthorities());
        reference.setCreatorId(creator);
        reference.setFactChecked(entity);

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
