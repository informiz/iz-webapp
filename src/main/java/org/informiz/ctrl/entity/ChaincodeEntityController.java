package org.informiz.ctrl.entity;

import org.informiz.auth.AuthUtils;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.FactCheckedEntity;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Set;


public abstract class ChaincodeEntityController<T extends ChainCodeEntity> {
    public static final String REVIEW_ATTR = "review";

    protected final ChaincodeEntityRepo<T> entityRepo;
    // TODO: chaincode DAO


    public ChaincodeEntityController(ChaincodeEntityRepo<T> entityRepo) {
        this.entityRepo = entityRepo;
    }

    protected abstract String getRedirectToEditPage(Long id);
    protected abstract String getEditPageTemplate();
    protected abstract void modelForReviewError(Model model, T current);

    protected String reviewEntity(Long id, Review review, BindingResult result, Model model, Authentication authentication) {
        T current = reviewEntity(id, review, authentication, result);

        if(current == null) {
            // Review successful, redirect back to edit page
            return getRedirectToEditPage(id);
        }
        // Failed to review, reloaded page presents errors in UI, just add necessary entities to the model
        modelForReviewError(model, current);
        return getEditPageTemplate();
    }

    protected T reviewEntity(Long id, Review review, Authentication authentication, BindingResult result) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid local entity id"));

        if (! result.hasErrors()) {
            String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());

            Review current = entity.getCheckerReview(checker);
            if (current != null) {
                current.setRating(review.getRating());
                current.setComment(review.getComment());
            } else {
                entity.addReview(new Review(entity, review.getRating(), review.getComment()));
            }
            entityRepo.save(entity);
            return null;
        }
        return entity;
    }

    protected String deleteReview(Long id, Long revId, BindingResult result, Model model, Authentication authentication) {
        T current = deleteReview(id, revId, result, authentication);

        if(current == null) {
            // Deletion successful, redirect back to edit page
            return getRedirectToEditPage(id);
        }
        // Failed to delete review, reloaded page presents errors in UI, just add necessary entities to the model
        modelForReviewError(model, current);
        return getEditPageTemplate();
    }


    protected T deleteReview(long id, Long revId, BindingResult result, Authentication authentication) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));

        if (! result.hasErrors()) {
            String checker = AuthUtils.getUserEntityId(authentication.getAuthorities());
            Review current = entity.getCheckerReview(checker);
            if (current != null && current.getId().equals(revId)) {
                entity.removeReview(current);
                entityRepo.save(entity);
            } // TODO: warn if no review or different id?
            return null;
        }
        return entity;
    }


    // TODO: standardize handling of review/reference/sourcing of entities (directly on entity? Concurrency issues?)
    protected <S extends FactCheckedEntity> void referenceEntity(S entity, Reference reference,
                                                                 Authentication authentication) {

        Set<Reference> references = entity.getReferences();
        String creator = AuthUtils.getUserEntityId(authentication.getAuthorities());
        reference.setCreatorId(creator);
        reference.setFactCheckedEntityId(entity.getEntityId());

        Reference current = references.stream().filter(ref ->
                ref.equals(reference)).findFirst().orElse(null);
        if (current != null) {
            current.setEntailment(reference.getEntailment());
            current.setDegree(reference.getDegree());
            current.setComment(reference.getComment());
        } else {
            current = new Reference(entity, reference);
        }
        entity.addReference(current);
        entityRepo.save((T)entity);
    }
}
