package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import java.io.Serializable;

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
    public interface ExistingHypothesisFromUI extends ExistingEntityFromUI {}
    public interface NewHypothesisFromUI{}


    @NotBlank(message = "Claim is mandatory", groups = {NewHypothesisFromUI.class, ExistingHypothesisFromUI.class, Default.class})
    @Size(max = 500, message = "Claim must be under 500 characters", groups = {NewHypothesisFromUI.class, ExistingHypothesisFromUI.class, Default.class})
    @Column(length = 500)
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
