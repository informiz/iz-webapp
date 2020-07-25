package org.informiz.repo.informi;

import org.informiz.model.InformiBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;

// TODO: extend PagingAndSortingRepository instead?
public interface InformiRepository extends ChaincodeEntityRepo<InformiBase> {

    InformiBase findByName(String name);
}
