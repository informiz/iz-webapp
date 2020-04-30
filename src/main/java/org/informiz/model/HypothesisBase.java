package org.informiz.model;

/* TODO: need this?
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A data-type managed by the hypothesis contract. A hypothesis consists of:
 * - a factual claim
 * - a locale
 * - the current reliability/confidence score
 * - supporting references
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class HypothesisBase {

    // Hypothesis's id on the ledger
    private String hid;

    private String claim;

    private Locale locale;

    private Score score;

    private Map<String, Float> reviews = new HashMap<>();

    private Map<String, String> references = new HashMap<>();


    public String getHid() {
        return hid;
    }

    private void setHid(String hid) {
        this.hid = hid;
    }

    public String getClaim() {
        return claim;
    }

    private void setClaim(String claim) {
        this.claim = claim;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Add a review by a fact-checker to this hypothesis
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this hypothesis before
     * @see Map#put(Object, Object)
     */
    public Float addReview(String fcid, float reliability) {
        return reviews.put(fcid, reliability);
    }

    /**
     * Remove a review by a fact-checker from this hypothesis
     * @param fcid the fact-checker's id
     * @return the score given by this fact-checker, if one was found
     * @see Map#remove(Object)
     */
    public Float removeReview(String fcid) {
        return reviews.remove(fcid);
    }

    public Map<String, Float> getReviews() {
        return reviews;
    }

    /**
     * Add a reference to this hypothesis
     * @param tid the reference-text's id on the ledger
     * @return the reference-text's id, if it was already assigned to the hypothesis
     * @see Map#put(Object, Object)
     */
    public String addReference(String tid) {
        return references.put(tid, tid);
    }

    /**
     * Remove a reference from this hypothesis
     * @param tid the reference-text's id
     * @return the reference-text's id, if it was assigned to the hypothesis
     * @see Map#remove(Object)
     */
    public Float removeReference(String tid) {
        return reviews.remove(tid);
    }

    public Map<String, String> getReferences() {
        return references;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        HypothesisBase other = (HypothesisBase) obj;

        return this.hid.equals(other.hid);
    }

    @Override
    public int hashCode() {
        return this.hid.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"claim\": \"%s\", \"score\": %s }", claim, score.toString());
    }
}
