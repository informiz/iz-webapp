package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

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
public final class HypothesisBase extends ChainCodeEntity implements Serializable {

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

    public boolean addReference(Reference ref) {
        return references.add(ref);
    }

    public boolean removeReference(Reference ref) {
        return references.remove(ref);
    }

    public Reference getReference(@NotNull Reference ref) {
        // TODO: more efficient way?
        Reference found = references.stream().filter(reference ->
                ref.equals(reference)).findFirst().orElse(null);
        return found;
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
