package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot.SavedSnapshotService;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
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
        final SavedSnapshot savedSnapshot = new SavedSnapshot.Builder()
                .setResultCount(resultCount)
                .setStateToken(Collections.singletonList("abc"))
                .setTitle(title)
                .build();

        final SavedSnapshot entity = savedSnapshotService.create(savedSnapshot);
        assertThat(entity.getId(), isA(Long.class));
        assertEquals(entity.getResultCount(), resultCount);
        assertThat(entity.getStateToken(), isA(List.class));
        assertEquals(entity.getStateToken().size(), 1);
        assertNotNull(entity.getId());

        entity.setQueryText("*");
        final SavedSnapshot updatedEntity = savedSnapshotService.update(entity);
        assertEquals(updatedEntity.getResultCount(), resultCount);
        assertThat(updatedEntity.getStateToken(), isA(List.class));
        assertEquals(updatedEntity.getStateToken().size(), 1);
        assertEquals(updatedEntity.getQueryText(), "*");

        final Set<SavedSnapshot> fetchedEntities = savedSnapshotService.getAll();
        assertEquals(fetchedEntities.size(), 1);
        assertEquals(fetchedEntities.iterator().next().getTitle(), title);

        savedSnapshotService.deleteById(updatedEntity.getId());
        assertEquals(savedSnapshotService.getAll().size(), 0);
    }

    @Test
    @Transactional
    public void getAllReturnsNothing() throws Exception {
        assertThat(savedSnapshotService.getAll(), is(empty()));
    }
    
}
