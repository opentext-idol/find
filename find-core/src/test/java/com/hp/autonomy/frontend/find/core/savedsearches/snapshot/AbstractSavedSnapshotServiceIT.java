package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;

public abstract class AbstractSavedSnapshotServiceIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SavedSnapshotService savedSnapshotService;

    @Test
    @DirtiesContext
    public void createFetchDelete() {
        final String title = "Any old saved snapshot";
        final Long resultCount = 100L;

        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();
        final ConceptClusterPhrase manhattanClusterPhraseOne = new ConceptClusterPhrase("manhattan", true, 0);
        final ConceptClusterPhrase manhattanClusterPhraseTwo = new ConceptClusterPhrase("mid-town", false, 0);
        conceptClusterPhrases.add(manhattanClusterPhraseOne);
        conceptClusterPhrases.add(manhattanClusterPhraseTwo);

        final SavedSnapshot savedSnapshot = new SavedSnapshot.Builder()
                .setResultCount(resultCount)
                .setStateToken(Collections.singletonList("abc"))
                .setConceptClusterPhrases(conceptClusterPhrases)
                .setQueryText("*")
                .setTitle(title)
                .build();

        final SavedSnapshot entity = savedSnapshotService.create(savedSnapshot);

        assertThat(entity.getId(), isA(Long.class));
        assertEquals(entity.getResultCount(), resultCount);
        assertThat(entity.getStateTokens(), isA(List.class));
        assertEquals(entity.getStateTokens().size(), 1);
        assertNotNull(entity.getId());
        assertThat(entity.getConceptClusterPhrases(), hasSize(2));

        conceptClusterPhrases.clear();
        final ConceptClusterPhrase jerseyClusterPhraseOne = new ConceptClusterPhrase("jersey", true, 0);
        conceptClusterPhrases.add(jerseyClusterPhraseOne);

        entity.setQueryText("*");
        entity.setConceptClusterPhrases(conceptClusterPhrases);

        SavedSnapshot updatedEntity = savedSnapshotService.update(entity);

        assertEquals(updatedEntity.getResultCount(), resultCount);
        assertThat(updatedEntity.getStateTokens(), isA(List.class));
        assertEquals(updatedEntity.getStateTokens().size(), 1);
        assertEquals(updatedEntity.getQueryText(), "*");

        final Set<ConceptClusterPhrase> updatedConceptClusters = updatedEntity.getConceptClusterPhrases();
        assertThat(updatedConceptClusters, hasSize(1));

        final ConceptClusterPhrase updatedConceptCluster = updatedConceptClusters.iterator().next();
        assertThat(updatedConceptCluster.getClusterId(), is(jerseyClusterPhraseOne.getClusterId()));
        assertThat(updatedConceptCluster.getPhrase(), is(jerseyClusterPhraseOne.getPhrase()));
        assertThat(updatedConceptCluster.isPrimary(), is(jerseyClusterPhraseOne.isPrimary()));

        // Mimic how the update method is likely to be called - with an entity without a user
        final SavedSnapshot updateInputEntity = new SavedSnapshot.Builder()
                .setTitle(title)
                .setId(entity.getId())
                .setQueryText("cat")
                .build();

        updatedEntity = savedSnapshotService.update(updateInputEntity);

        assertEquals(updatedEntity.getQueryText(), "cat");
        assertNotNull(updatedEntity.getUser());

        final Set<SavedSnapshot> fetchedEntities = savedSnapshotService.getAll();

        final SavedSnapshot fetchedEntity = fetchedEntities.iterator().next();
        assertEquals(fetchedEntities.size(), 1);
        assertEquals(fetchedEntity.getTitle(), title);

        final ConceptClusterPhrase fetchedConceptClusterPhrase = fetchedEntity.getConceptClusterPhrases().iterator().next();
        assertThat(fetchedConceptClusterPhrase.getClusterId(), is(jerseyClusterPhraseOne.getClusterId()));
        assertThat(fetchedConceptClusterPhrase.getPhrase(), is(jerseyClusterPhraseOne.getPhrase()));
        assertThat(fetchedConceptClusterPhrase.isPrimary(), is(jerseyClusterPhraseOne.isPrimary()));

        savedSnapshotService.deleteById(updatedEntity.getId());

        assertEquals(savedSnapshotService.getAll().size(), 0);
    }

    @Test
    @Transactional
    public void getAllReturnsNothing() throws Exception {
        assertThat(savedSnapshotService.getAll(), is(empty()));
    }
}
