package org.informiz.repo;

import org.informiz.WithCustomAuth;
import org.informiz.model.CitationBase;
import org.informiz.model.ModelTestUtils;
import org.informiz.repo.citation.CitationRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.informiz.auth.InformizGrantedAuthority.ROLE_MEMBER;
import static org.informiz.model.ModelTestUtils.getPopulatedCitation;
import static org.informiz.model.Score.CONFIDENCE_BOOST;
import static org.junit.jupiter.api.Assertions.*;


public class CitationsRepositoryTest extends ChaincodeEntityRepoTest<CitationBase, CitationRepository> {

    protected void initEntities() {
        chaincodeEntity = getPopulatedCitation(null);
        rev = ModelTestUtils.getPopulatedReview(chaincodeEntity, null);
        chaincodeEntity.addReview(rev);
    }


    //FindByID
    @Test
    @Disabled("Query Count is Zero, Fix Test")
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByID_thenReturnPersistedCitationFullData() {
        CitationBase found = chaincodeEntityRepo.findById(chaincodeEntity.getId()).get();

        verifyLoading(found, 1, new String[]{"reviews", "sources"}, new String[]{"references"});
        verifyCitation(found);
    }

    //No FindByName

    //FindAll
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindAll_thenReturnPersistedCitationPreviewData() {
        List<CitationBase> all = StreamSupport
                .stream(chaincodeEntityRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(all.size(), 1);
        CitationBase found = all.get(0);

        verifyLoading(found, 1, new String[]{"reviews"}, new String[]{"references", "sources"});
        verifyCitation(found);
    }

    //FindByEntityId
    @Test
    @WithCustomAuth(role = {ROLE_MEMBER})
    public void whenFindByEntityId_thenReturnPersistedCitationFullData() {
        CitationBase found = chaincodeEntityRepo.findByEntityId(chaincodeEntity.getEntityId());

        verifyLoading(found, 1, new String[]{"reviews", "sources"}, new String[]{"references"});
        verifyCitation(found);
    }


    private void verifyCitation(CitationBase found) {
        assertEquals(chaincodeEntity.getLink(), found.getLink());
        assertEquals(chaincodeEntity.getText(), found.getText());
        assertEquals(1, found.getReviews().size());
        assertEquals(rev.getRating(), found.getScore().getReliability().floatValue(), 0.001);
        assertEquals(CONFIDENCE_BOOST, found.getScore().getConfidence().floatValue(), 0.001);
    }

    private void verifyLoading(CitationBase found, int expectedQueryCount, String[] loaded, String[] unLoaded) {
        assertEquals(expectedQueryCount, statistics.getQueryExecutionCount());
        for (String prop : loaded)
            assertTrue(unitUtil.isLoaded(found, prop), String.format("%s not loaded", prop));

        for (String prop : unLoaded)
            assertFalse(unitUtil.isLoaded(found, prop), String.format("%s is loaded", prop));

        // TODO: can also verify cascading on update/delete with getEntity[Update/Delete]Count
    }
}