package org.informiz.ctrl.entity;

import org.informiz.model.Reference;

import java.util.Set;

public interface EntityWithReferences {

    Set<Reference> getReferences();
}
