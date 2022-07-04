package org.informiz.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ReferencedEntity extends ChainCodeEntity {
    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL, orphanRemoval=true)
    protected Set<Reference> references;

    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public boolean removeReference(Long referenceId) {
        List<Reference> snapshot = new ArrayList(references);
        Reference ref = snapshot.stream().filter(reference ->
                referenceId.equals(reference.getId())).findFirst().orElse(null);

        if (ref != null)
            return references.remove(ref);

        return false;
    }

}
