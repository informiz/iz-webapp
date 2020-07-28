package org.informiz.repo.source;

import org.informiz.model.SourceBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;

public interface SourceRepository extends ChaincodeEntityRepo<SourceBase> {

    SourceBase findByName(String name);
}
