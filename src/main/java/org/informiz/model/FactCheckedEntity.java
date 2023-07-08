package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonView(Utils.Views.EntityDefaultView.class)
//@MappedSuperclass
public abstract class FactCheckedEntity extends ChainCodeEntity {

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval=true)
    @JoinColumn(name = "fact_checked_entity_id", referencedColumnName = "entity_id")
    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }


    public boolean addReference(Reference reference) {
        boolean bool = false;
        synchronized (references) {
            bool = getReferences().add(reference);
        }
        return bool;
    }

    public boolean removeReference(Reference reference) {
        boolean bool = false;
        synchronized (references) {
            bool = getReferences().remove(reference);
        }
        return bool;
    }

    public boolean removeReference(Long referenceId) {
        List<Reference> snapshot = new ArrayList(references);
        Reference ref = snapshot.stream().filter(reference ->
                referenceId.equals(reference.getId())).findFirst().orElse(null);

        if (ref != null)
            return removeReference(ref);

        return false;
    }

}
