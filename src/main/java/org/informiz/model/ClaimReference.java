package org.informiz.model;

import javax.persistence.*;
import java.io.Serializable;

@Table(name="claim_reference")
@Entity
public class ClaimReference extends InformizEntity implements Serializable {

    static final long serialVersionUID = 1L;

    public enum Entailment {
        SUPPORTS, CONTRADICTS, IRRELEVANT;
    }

    // The claim (hypothesis) entity-id
    @Column(name = "claim_id")
    private String claim;

    // The reference entity-id
    @Column(name = "reference_id")
    private String reference;

    @Enumerated(EnumType.ORDINAL)
    private Entailment entailment;

    @Column
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claimId) {
        this.claim = claimId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Entailment getEntailment() {
        return entailment;
    }

    public void setEntailment(Entailment entailment) {
        this.entailment = entailment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
