package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Table(name="hypothesis")
@Entity
@JsonView(Utils.Views.EntityDefaultView.class)
@NamedEntityGraphs({
        @NamedEntityGraph(
                name= HypothesisBase.CLAIM_PREVIEW,
                attributeNodes={
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score")
                }
        ),
        @NamedEntityGraph(
                name= HypothesisBase.CLAIM_DATA,
                attributeNodes={
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score"),
                        @NamedAttributeNode("references"),
                        @NamedAttributeNode("sources")
                }
        )

})
public final class HypothesisBase extends FactCheckedEntity implements Serializable {

    static final long serialVersionUID = 3L ;
    public static final String CLAIM_PREVIEW = "claim-with-reviews";
    public static final String CLAIM_DATA = "claim-full-data";

    /**
     * Validation group for add/edit hypothesis through the UI (most fields will not be initialized)
     */
    public interface HypothesisFromUI {}

    @NotBlank(message = "Claim is mandatory", groups = {HypothesisFromUI.class, Default.class})
    private String claim;

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public void edit(HypothesisBase other) {
        super.edit(other);
        this.setClaim(other.getClaim());
    }
}
