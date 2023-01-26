package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="hypothesis")
@Entity
public final class HypothesisBase extends FactCheckedEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Claim is mandatory")
    private String claim;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL, orphanRemoval=true)
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
