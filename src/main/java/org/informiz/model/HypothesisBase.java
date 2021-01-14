package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.informiz.ctrl.entity.EntityWithReferences;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="hypothesis")
@Entity
public final class HypothesisBase extends ChainCodeEntity implements Serializable, EntityWithReferences {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Claim is mandatory")
    private String claim;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL)
    protected Set<SourceRef> sources;

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL)
    protected Set<Reference> references;


    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public Set<Reference> getReferences() {
        return references;
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
