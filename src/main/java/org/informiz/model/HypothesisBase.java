package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="hypothesis")
@Entity
@NamedEntityGraphs({
        @NamedEntityGraph(
                name= HypothesisBase.CLAIM_PREVIEW,
                includeAllAttributes=true,
                attributeNodes={
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score")
                }
        ),
        @NamedEntityGraph(
                name= HypothesisBase.CLAIM_DATA,
                includeAllAttributes=true,
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

    @NotBlank(message = "Claim is mandatory")
    private String claim;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL, orphanRemoval=true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    protected Set<SourceRef> sources;

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public Set<SourceRef> getSources() {
        return sources;
    }

    public void setSources(Set<SourceRef> sources) {
        this.sources = sources;
    }

    public boolean removeSource(Long srcRefId) {
        List<SourceRef> snapshot = new ArrayList(sources);
        SourceRef ref = snapshot.stream().filter(reference ->
                srcRefId.equals(reference.getId())).findFirst().orElse(null);

        if (ref != null)
            return sources.remove(ref);
        return false;
    }

    public boolean addSource(SourceRef src) {
        return sources.add(src);
    }

    public void edit(HypothesisBase other) {
        super.edit(other);
        this.setClaim(other.getClaim());
    }
}
