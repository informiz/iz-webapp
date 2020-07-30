package org.informiz.model;

/* TODO: need this?
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

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

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL)
    protected Set<Reference> references;


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

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public Set<Reference> getReferences() {
        return references;
    }

    public boolean addReference(Reference ref) {
        return references.add(ref);
    }

    public Reference getReference(@NotNull Reference ref) {
        // TODO: more efficient way?
        Reference found = references.stream().filter(reference ->
                ref.equals(reference)).findFirst().orElse(null);
        return found;
    }
    public boolean removeReference(Reference ref) {
        return references.remove(ref);
    }

    public void edit(HypothesisBase other) {
        this.setClaim(other.getClaim());
        this.setSid(other.getSid());
        this.setLocale(other.getLocale());
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }
}
