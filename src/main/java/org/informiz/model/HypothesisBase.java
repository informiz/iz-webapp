package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="hypothesis")
@Entity
public final class HypothesisBase extends ReferencedEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Claim is mandatory")
    private String claim;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL)
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

    public boolean removeSource(SourceRef src) {
        return sources.remove(src);
    }

    public boolean addSource(SourceRef src) {
        return sources.add(src);
    }

    public void edit(HypothesisBase other) {
        super.edit(other);
        this.setClaim(other.getClaim());
    }
}
