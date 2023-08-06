package org.informiz.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        setMetaData(id, ref);
        return ref;
    }

    @NotNull
    public static SourceRef getPopulatedSrcReference(@Nullable Long id) {
        SourceRef ref = new SourceRef();

        ref.setSourcedId("Test");
        ref.setSrcEntityId("Test");
        ref.setLink("https://informiz.org");
        ref.setDescription("Test");

        setMetaData(id, ref);
        return ref;
    }

    @NotNull
    public static InformiBase getPopulatedInformi(@Nullable Long id) {
        InformiBase informi = new InformiBase();
        informi.setEntityId("TestInformiIdLongerThanTwentyFive");
        informi.setName("Test Informi");
        informi.setDescription("Test description for Informi");
        informi.setMediaPath("https://server.com/path/to/image.jpg");

        setMetaData(id, informi);
        return informi;
    }
    @NotNull
    public static HypothesisBase getPopulatedHypothesis(@Nullable Long id) {
        HypothesisBase hypothesis = new HypothesisBase();
        hypothesis.setEntityId("TestHypothesisIdLongerThanTwentyFive");
        hypothesis.setClaim("Test Claim");

        setMetaData(id, hypothesis);
        return hypothesis;
    }

    @NotNull
    public static CitationBase getPopulatedCitation(@Nullable Long id) {
        CitationBase citation = new CitationBase();
        citation.setEntityId("TestCitationIdLongerThanTwentyFive");
        citation.setLink("https://server.com/path/to/image.jpg");
        citation.setText("Citation Test");

        setMetaData(id, citation);
        return citation;
    }

    @NotNull
    public static SourceBase getPopulatedSourceBase(@Nullable Long id) {
        SourceBase sourceBase = new SourceBase();
        sourceBase.setName("SourceBaseTest");
        sourceBase.setEntityId("TestSourceBaseIdLongerThanTwentyFive");
        sourceBase.setLink("https://server.com/path/to/image.jpg");
        sourceBase.setSrcType(SourceBase.SourceType.PERSON);
        sourceBase.setDescription("Test Description");
        setMetaData(id, sourceBase);
        return sourceBase;
    }

    private static void setMetaData(Long id, InformizEntity izEntity) {
        izEntity.setId(id);
        izEntity.setCreatorId("test");
        izEntity.setOwnerId("test");
        izEntity.setCreatedTs(12345l);
        izEntity.setUpdatedTs(12345l);
    }


    public static String getValidUrl(int length) throws UnsupportedEncodingException {
        return String.format("https://www.server.com/%s",
                URLEncoder.encode(RandomStringUtils.random(length),
                        StandardCharsets.UTF_8.toString()));
    }
}
