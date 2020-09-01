package org.informiz.repo.checker;

import org.informiz.auth.InformizGrantedAuthority;
import org.informiz.model.FactCheckerBase;
import org.springframework.data.repository.CrudRepository;

// TODO: extend PagingAndSortingRepository instead?
public interface FactCheckerRepository extends CrudRepository<FactCheckerBase, Long> {

    // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************
    default void init() {
        if (count() == 0) {
            save(new FactCheckerBase("Albert", "ashiagborayi@gmail.com", "https://www.linkedin.com/in/albert-ayi-ashiagbor-a0233815a/"));
            save(new FactCheckerBase("Daniel", "danosaf291@gmail.com", "https://www.linkedin.com/in/daniel-osarfo-8b21a482/"));
            save(new FactCheckerBase("Richard", "richardtm905@gmail.com", "https://www.linkedin.com/in/niraamit/"));
            save(new FactCheckerBase("Kim", "kimberly@informiz.org", "https://www.linkedin.com/in/kimberly-caesar-bb204340/"));
            save(new FactCheckerBase("Nira", "nira@informiz.org", "https://www.linkedin.com/in/niraamit/"));

            InformizGrantedAuthority.setCheckers(findAll());
        }
    }
    // TODO: *************************** TESTING, REMOVE THIS!!!! ***************************

    FactCheckerBase findByEntityId(String entityId);

    FactCheckerBase findById(long id);

    FactCheckerBase findByName(String name);

    FactCheckerBase findByEmail(String email);
}
