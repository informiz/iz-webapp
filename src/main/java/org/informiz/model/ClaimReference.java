package org.informiz.model;

import javax.persistence.*;
import java.io.Serializable;

@Table(name="claim_reference")
@Entity
public class ClaimReference implements Serializable {

    static final long serialVersionUID = 1L;

    public enum Entailment {
        ENTAILS, CONTRADICTS, NONE;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    // The claim (hypothesis) entity-id
    @Column(name = "entity_id")
    private String entityId;

    // The reference entity-id
    private String reference;

    @Enumerated(EnumType.ORDINAL)
    private Entailment entailment;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String claimId) {
        this.entityId = claimId;
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
}
