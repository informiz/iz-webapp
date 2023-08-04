package org.informiz.ctrl.entity;

import org.informiz.auth.AuthUtils;
import org.informiz.model.*;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.annotation.Nullable;


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

            Review current = entity.getCheckerReview(checker, review.getId());
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
            String checker = authentication.getName();
            Review current = entity.getCheckerReview(checker, revId);
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
            Reference toAdd = new Reference(reference);

            if (reference.getId() != null)
                entity.removeReference(reference.getId(), authentication.getName());

            entity.addReference(toAdd);
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
            String owner = authentication.getName();
            entity.removeReference(refId, owner);
            entityRepo.save((T)entity);
            return null;
        }
        return entity;
    }

    protected String sourceForEntity(Long id, SourceRef srcRef, SourceBase source, BindingResult result, Model model, Authentication authentication) {
        T current = sourceForEntity(id, srcRef, source, result, authentication);

        if(current == null) {
            return successfulEdit(model, id, srcRef);
        }
        return failedEdit(model, result, current, srcRef);
    }

    protected T sourceForEntity(Long id, SourceRef srcRef, SourceBase source, BindingResult result, Authentication authentication) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid local entity id"));

        if (! result.hasErrors()) {
            try {
                SourceRef toAdd = new SourceRef(srcRef);

                if (srcRef.getId() != null)
                    entity.removeSource(srcRef.getId(), authentication.getName());

                entity.addSource(toAdd);
                entityRepo.save(entity);
                return null;
            } catch (IllegalArgumentException e) {
                result.addError(new FieldError("source", "link", e.getMessage()));
            }
        }
        return entity;
    }

    protected String deleteSrcReference(Long id, Long srcRefId, BindingResult result, Model model, Authentication authentication) {
        T current = deleteSrcReference(id, srcRefId, result, authentication);

        if(current == null) {
            return successfulEdit(model, id, (Review) model.getAttribute(REVIEW_ATTR));
        }
        return failedEdit(model, result, current, (Review) model.getAttribute(REVIEW_ATTR));
    }

    protected T deleteSrcReference(long id, Long srcRefId, BindingResult result, Authentication authentication) {
        T entity = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid entity id"));
        String owner = authentication.getName();

        if (! result.hasErrors()) {
            entity.removeSource(srcRefId, owner);
            entityRepo.save(entity);
            return null;
        }
        return entity;
    }
}
