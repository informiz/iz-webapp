package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.model.ModelTestUtils;
import org.informiz.model.SourceBase;
import org.informiz.repo.source.SourceRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.informiz.model.ModelTestUtils.getPopulatedSourceBase;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.*;


public class SourceRepositoryTest extends ChaincodeEntityRepoTest<SourceBase, SourceRepository> {

    protected void initEntities() {
        chaincodeEntity = getPopulatedSourceBase(null);
        rev = ModelTestUtils.getPopulatedReview(chaincodeEntity, null);
        chaincodeEntity.addReview(rev);
    }


    //FindByID
    @Test
    @Disabled("Query Count is Zero, Fix Test")
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByID_thenReturnPersistedSourceFullData() {
        SourceBase found = chaincodeEntityRepo.findById(chaincodeEntity.getId()).get();

        verifyLoading(found, 1, new String[]{"reviews", "references"}, new String[]{"sources"});
        verifyInformi(found);
    }


    //FindByName
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByName_thenReturnPersistedInformiFullData() {
        SourceBase found = chaincodeEntityRepo.findByName(chaincodeEntity.getName());

        verifyLoading(found, 1, new String[]{"reviews"}, new String[]{"references", "sources"});
        verifyInformi(found);
    }

    //FindAll
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnPersistedSourcePreviewData() {
        List<SourceBase> all = StreamSupport
                .stream(chaincodeEntityRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        SourceBase found = all.get(0);

        verifyLoading(found, 1, new String[]{"reviews"}, new String[]{"references", "sources"});
        verifyInformi(found);
    }

    //FindByEntityId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByEntityId_thenReturnPersistedInformiFullData() {
        SourceBase found = chaincodeEntityRepo.findByEntityId(chaincodeEntity.getEntityId());

        verifyLoading(found, 1, new String[]{"reviews"}, new String[]{"references", "sources"});
        verifyInformi(found);
    }


    private void verifyInformi(SourceBase found) {
        assertEquals(chaincodeEntity.getName(), found.getName());
        assertEquals(1, found.getReviews().size());
        assertEquals(rev.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }

    private void verifyLoading(SourceBase found, int expectedQueryCount, String[] loaded, String[] unLoaded) {
        assertEquals(expectedQueryCount, statistics.getQueryExecutionCount());
        for (String prop : loaded)
            assertTrue(unitUtil.isLoaded(found, prop), String.format("%s not loaded", prop));

        for (String prop : unLoaded)
            assertFalse(unitUtil.isLoaded(found, prop), String.format("%s is loaded", prop));

        // TODO: can also verify cascading on update/delete with getEntity[Update/Delete]Count
    }
}