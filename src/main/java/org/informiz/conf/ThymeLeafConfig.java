package org.informiz.conf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.model.SourceRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * A helper class for HTML rendering, contains helper-methods used in Thymeleaf templates
 */
@Configuration
public class ThymeLeafConfig {

    private static class ThymeleafUtils {
        @Autowired
        private HttpServletRequest request;

        /**
         * Check if there is an error related to editing a reference.
         *
         * @param reference the reference represented by the form
         * @param errReference the reference that caused the error
         * @return true iff editing the given reference is the cause of the error
         */
        public boolean isRefFormError(@NotNull Reference reference, @NotNull Reference errReference) {
            return request.getRequestURI().contains("/reference/") && reference.getId() == errReference.getId();
        }

        /**
         * Check if there is an error related to adding a reference.
         *
         * @param reference the reference represented by the form
         * @param errReference the reference that caused the error
         * @return true iff adding a reference is the cause of the error
         */
        public boolean isAddRefFormError(@NotNull Reference reference, @NotNull Reference errReference) {
            return isRefFormError(reference, errReference) &&
                    (reference.getId() == null || reference.getId().equals(0l));
        }

        /**
         * Check if there is an error related to review add/edit.
         * @implNote An error while editing a review also causes the "add review" modal to "show", but it actually
         * triggers the same modal with the same content.
         *
         * @param review the review represented by the form
         * @param errReview the review that caused the error
         * @return true if the given review (form) is the cause of the error
         */
        public boolean isReviewFormError(@NotNull Review review, @NotNull Review errReview) {
            return request.getRequestURI().contains("/review/") && review.getId() == errReview.getId();
        }

        /**
         * Check if there is an error related to editing a source-reference.
         *
         * @param sourceRef the source-reference represented by the form
         * @param errSrcRef the source-reference that caused the error
         * @return true iff editing the given source-reference is the cause of the error
         */
        public boolean isSourceRefFormError(@NotNull SourceRef sourceRef, @NotNull SourceRef errSrcRef) {
            return request.getRequestURI().contains("/source-ref/") && sourceRef.getId() == errSrcRef.getId();
        }

        /**
         * Check if there is an error related to adding a source-reference.
         *
         * @param sourceRef the source-reference represented by the form
         * @param errSrcRef the source-reference that caused the error
         * @return true iff adding a source-reference is the cause of the error
         */
        public boolean isAddSourceRefFormError(@NotNull SourceRef sourceRef, @NotNull SourceRef errSrcRef) {
            return isSourceRefFormError(sourceRef, errSrcRef) &&
                    (sourceRef.getId() == null || sourceRef.getId().equals(0l));
        }
    }

    @Bean(name = "tlUtils")
    static ThymeleafUtils tlUtilsBean() {
        return new ThymeleafUtils();
    }
}
