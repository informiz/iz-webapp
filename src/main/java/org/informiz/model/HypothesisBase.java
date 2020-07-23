package org.informiz.model;

/* TODO: need this?
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
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
@Table(name="hypothesis")
@Entity
public final class HypothesisBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Claim is mandatory")
    private String claim;

    @NotBlank(message = "Source is mandatory")
    private String sid;

    @NotNull(message = "Locale is mandatory")
    private Locale locale;

    @ElementCollection
    @CollectionTable(name = "claim_reference")
    @MapKeyJoinColumn(name="entity_id", referencedColumnName="claim_id")
    @MapKeyColumn(name = "reference_id")
    @Column(name = "entailment")
    private Map<String, ClaimReference.Entailment> references = new HashMap<>();

    // TODO: can other claims be references as well?

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setReferences(Map<String, ClaimReference.Entailment> references) {
        this.references = references;
    }

    /**
     * Add a reference to this hypothesis
     * @param tid the reference-text's id on the ledger
     * @return the previous entailment value, if the reference was already assigned to the hypothesis
     * @see Map#put(Object, Object)
     */
    public ClaimReference.Entailment addReference(String tid, ClaimReference.Entailment entailment) {

        return references.put(tid, entailment);
    }

    /**
     * Remove a reference from this hypothesis
     * @param tid the reference-text's id
     * @return the entailment value, if the reference was assigned to the hypothesis
     * @see Map#remove(Object)
     */
    public ClaimReference.Entailment removeReference(String tid) {
        return references.remove(tid);
    }

    public Map<String, ClaimReference.Entailment> getReferences() {
        return references;
    }


    public void edit(HypothesisBase other) {
        this.setClaim(other.getClaim());
        this.setSid(other.getSid());
        this.setLocale(other.getLocale());
        this.setReferences(new HashMap<>(other.getReferences()));
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }


}
