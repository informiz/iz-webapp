package org.informiz.model;

import javax.persistence.*;
import java.io.Serializable;

@Table(name="informi_claim")
@Entity
public class InformiClaim extends InformizEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // The informi entity-id
    @Column(name = "informi_id")
    private String infromi;

    // The claim (hypothesis) entity-id
    @Column(name = "claim_id")
    private String claim;

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claimId) {
        this.claim = claimId;
    }

    public String getInfromi() {
        return infromi;
    }

    public void setInfromi(String infromiId) {
        this.infromi = infromiId;
    }

}
