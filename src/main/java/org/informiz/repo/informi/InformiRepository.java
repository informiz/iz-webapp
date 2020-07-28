package org.informiz.repo.informi;

import org.informiz.model.InformiBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;

public interface InformiRepository extends ChaincodeEntityRepo<InformiBase> {

    InformiBase findByName(String name);
}
