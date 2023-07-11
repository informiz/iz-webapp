package org.informiz.ctrl.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.informiz.auth.AuthUtils;
import org.informiz.model.*;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.annotation.Nullable;
import java.util.Set;


public abstract class ChaincodeEntityController<T extends ChainCodeEntity> {
    public static final String REVIEW_ATTR = "review";
    public static final String REFERENCE_ATTR = "reference";
    public static final String SOURCE_ATTR = "source";

    protected final ChaincodeEntityRepo<T> entityRepo;
    // TODO: chaincode DAO


    public ChaincodeEntityController(ChaincodeEntityRepo<T> entityRepo) {
        this.entityRepo = entityRepo;
    }

    protected String getRedirectToEditPage(Long id) { return null; }
    protected String getEditPageTemplate() { return null; }

    protected void modelForError(Model model, T current) {
        model.addAttribute(JsonView.class.getName(), Utils.Views.EntityData.class);
        if (! model.containsAttribute(REVIEW_ATTR)) model.addAttribute(REVIEW_ATTR, new Review());
    };

    protected String successfulEdit(Model model, Long localId, @Nullable InformizEntity userInput) {
        // Assuming redirect back to edit-page
        return getRedirectToEditPage(localId);
    }

    protected String failedEdit(Model model, BindingResult result, T current, InformizEntity userInput) {
        modelForError(model, current);
        // Assuming reloaded page presents errors in UI
        return getEditPageTemplate();
    }

    protected String reviewEntity(Long id, Review review, BindingResult result, Model model, Authentication authentication) {
        T current = reviewEntity(id, review, authentication, result);

        if(current == null) {
            return successfulEdit(model, id, review);
        }
        return failedEdit(model, result, current, review);
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
            return successfulEdit(model, id, (Review) model.getAttribute(REVIEW_ATTR));
        }
        return failedEdit(model, result, current, (Review) model.getAttribute(REVIEW_ATTR));
    }


    protected T deleteReview(long id, Long revId, BindingResult result, Authentication authentication) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));

        if (! result.hasErrors()) {
            String checker = AuthUtils.getUserEntityId(authentication.getAuthorities()); // TODO: auth.getName() ?
            Review current = entity.getCheckerReview(checker);
            if (current != null && current.getId().equals(revId)) {
                entity.removeReview(current);
                entityRepo.save(entity);
            } // TODO: warn if no review or different id?
            return null;
        }
        return entity;
    }


    @SuppressWarnings("unchecked")
    protected <S extends FactCheckedEntity> String referenceEntity(Long id, Reference reference, BindingResult result, Model model, Authentication authentication) {
        S current = referenceEntity(id, reference, authentication, result);

        if(current == null) {
            return successfulEdit(model, id, reference);
        }
        return failedEdit(model, result, (T)current, reference);
    }


    @SuppressWarnings("unchecked")
    protected <S extends FactCheckedEntity> S referenceEntity(Long id, Reference reference,
                                                                 Authentication authentication, BindingResult result) {

        S entity;
        try {
            entity = (S) entityRepo.loadByLocalId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Unable to reference entity", e);
        }

        if (! result.hasErrors()) {
            Set<Reference> references = entity.getReferences();
            String creator = AuthUtils.getUserEntityId(authentication.getAuthorities()); // TODO: auth.getName() ?
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
            return null;
        }

        return entity;
    }


    @SuppressWarnings("unchecked")
    protected <S extends FactCheckedEntity> String deleteReference(Long id, Long refId, BindingResult result, Model model, Authentication authentication) {
        S current = deleteReference(id, refId, result, authentication);

        if(current == null) {
            return successfulEdit(model, id, (Reference) model.getAttribute(REFERENCE_ATTR));
        }
        return failedEdit(model, result, (T)current, (Reference) model.getAttribute(REFERENCE_ATTR));
    }


    @SuppressWarnings("unchecked")
    protected <S extends FactCheckedEntity> S deleteReference(long id, Long refId, BindingResult result, Authentication authentication) {
        S entity;
        try {
            entity = (S) entityRepo.loadByLocalId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Unable to add source to entity", e);
        }

        if (! result.hasErrors()) {
            entity.removeReference(refId);
            entityRepo.save((T)entity);
            return null;
        }
        return entity;
    }



}
