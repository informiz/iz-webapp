package org.informiz.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ModelTestUtils {

    @NotNull
    public static Review getPopulatedReview() {
        return getPopulatedReview(null);
    }

    @NotNull
    public static Review getPopulatedReview(@Nullable ChainCodeEntity reviewed) {
        return getPopulatedReview(reviewed, 1l);
    }

    @NotNull
    public static Review getPopulatedReview(@Nullable ChainCodeEntity reviewed, @Nullable Long id) {
        Review review = new Review();
        review.setRating(0.8f);
        if (reviewed == null) {
            review.setReviewedEntityId("test");
        } else {
            review.setReviewedEntityId(reviewed.getEntityId());
        }
        review.setId(id);
        setMetaData(id, review);
        return review;
    }

    @NotNull
    public static Reference getPopulatedReference(@Nullable FactCheckedEntity referenced,
                                                  @Nullable String refEntityId,
                                                  @Nullable Long id) {
        Reference ref = new Reference();
        ref.setFactCheckedEntityId(referenced == null ? "test" : referenced.getEntityId());
        ref.setRefEntityId(refEntityId);
        ref.setEntailment(Reference.Entailment.SUPPORTS);
        ref.setDegree(0.9f);

        ref.setId(id);
        setMetaData(id, ref);
        return ref;
    }

    @NotNull
    public static InformiBase getPopulatedInformi(@Nullable Long id) {
        InformiBase informi = new InformiBase();
        informi.setEntityId("TestInformiId");
        informi.setName("Test Informi");
        informi.setDescription("Test description for Informi");
        informi.setMediaPath("https://server.com/path/to/image.jpg");

        informi.setLocalId(id);
        setMetaData(id, informi);
        return informi;
    }


    private static void setMetaData(@org.jetbrains.annotations.Nullable Long id, InformizEntity izEntity) {
        izEntity.setCreatorId("test");
        izEntity.setOwnerId("test");
        izEntity.setCreatedTs(12345l);
        izEntity.setUpdatedTs(12345l);
    }


}
