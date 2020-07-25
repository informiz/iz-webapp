package org.informiz.repo.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.model.SourceBase;
import org.informiz.repo.entity.ChaincodeEntityRepo;
import org.springframework.data.repository.CrudRepository;

public interface HypothesisRepository extends ChaincodeEntityRepo<HypothesisBase> {

}
