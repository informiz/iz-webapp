package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.model.InformiBase;
import org.informiz.model.ModelTestUtils;
import org.informiz.repo.informi.InformiRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.informiz.model.ModelTestUtils.getPopulatedInformi;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.*;


public class InformiRepositoryTest extends ChaincodeEntityRepoTest<InformiBase, InformiRepository> {

    protected void initEntities() {
        chaincodeEntity = getPopulatedInformi(null);
        rev = ModelTestUtils.getPopulatedReview(chaincodeEntity, null);
        chaincodeEntity.addReview(rev);
    }

    //FindByID
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByID_thenReturnPersistedInformiFullData() {
        InformiBase found = chaincodeEntityRepo.findById(chaincodeEntity.getId()).get();

        verifyLoading(found, 1, new String[]{"reviews", "references"}, new String[]{"sources"});
        verifyInformi(found);
    }


    //FindByName

    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByName_thenReturnPersistedInformiFullData() {
        InformiBase found = chaincodeEntityRepo.findByName(((InformiBase) chaincodeEntity).getName());

        verifyLoading(found, 1, new String[]{"reviews", "references"}, new String[]{"sources"});
        verifyInformi(found);
    }


    //FindAll
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnPersistedInformiPreviewData() {
        List<InformiBase> all = StreamSupport
                .stream(chaincodeEntityRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        InformiBase found = all.get(0);

        verifyLoading(found, 1, new String[]{"reviews"}, new String[]{"references", "sources"});
        verifyInformi(found);
    }

    //FindByEntityId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByEntityId_thenReturnPersistedInformiFullData() {
        InformiBase found = chaincodeEntityRepo.findByEntityId(chaincodeEntity.getEntityId());

        verifyLoading(found, 1, new String[]{"reviews", "references"}, new String[]{"sources"});
        verifyInformi(found);
    }


    private void verifyInformi(InformiBase found) {
        assertEquals(chaincodeEntity.getName(), found.getName());
        assertEquals(1, found.getReviews().size());
        assertEquals(rev.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }

    private void verifyLoading(InformiBase found, int expectedQueryCount, String[] loaded, String[] unLoaded) {
        assertEquals(expectedQueryCount, statistics.getPrepareStatementCount());
        for (String prop : loaded)
            assertTrue(unitUtil.isLoaded(found, prop), String.format("%s not loaded", prop));

        for (String prop : unLoaded)
            assertFalse(unitUtil.isLoaded(found, prop), String.format("%s is loaded", prop));

        // TODO: can also verify cascading on update/delete with getEntity[Update/Delete]Count
    }
}