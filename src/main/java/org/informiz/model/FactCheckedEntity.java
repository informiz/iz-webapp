package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@MappedSuperclass
public abstract class FactCheckedEntity extends ChainCodeEntity {

    @OneToMany(mappedBy = "factChecked",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval=true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    protected Set<Reference> references = new HashSet<>();

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
