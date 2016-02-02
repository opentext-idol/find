/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.savedsearches.savedquery.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.savedquery.SavedQueryService;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractSavedQueryServiceIT extends AbstractFindIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private SavedQueryService savedQueryService;

    @Test
    public void createFetchUpdateDelete() {
        final String title = "Any old saved search";
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setTitle(title)
                .build();

        final SavedQuery entity = savedQueryService.create(savedQuery);
        assertThat(entity.getId(), isA(Long.class));
        assertNotNull(entity.getId());

        entity.setQueryText("*");
        SavedQuery updatedEntity = savedQueryService.update(entity);
        assertEquals(updatedEntity.getQueryText(), "*");

        // Mimic how the update method is likely to be called - with an entity without a user
        final SavedQuery updateInputEntity = new SavedQuery.Builder()
                .setTitle(title)
                .setId(entity.getId())
                .setQueryText("cat")
                .build();
        updatedEntity = savedQueryService.update(updateInputEntity);
        assertEquals(updatedEntity.getQueryText(), "cat");
        assertNotNull(updatedEntity.getUser());

        final Set<SavedQuery> fetchedEntities = savedQueryService.getAll();
        assertEquals(fetchedEntities.size(), 1);
        assertEquals(fetchedEntities.iterator().next().getTitle(), title);

        savedQueryService.deleteById(updatedEntity.getId());
        assertEquals(savedQueryService.getAll().size(), 0);
    }

    @Test
    @Transactional
    public void getAllReturnsNothing() throws Exception {
        assertThat(savedQueryService.getAll(), is(empty()));
    }

    @Test
    public void checkUserAuditDataInserted() {
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setTitle("title")
                .build();

        savedQueryService.create(savedQuery);

        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getUser().getUserId(), isA(Long.class));
        assertNotNull(savedQuery.getUser().getUserId());
    }

    @Test
    public void checkTimeAuditDataInsertedUpdated() {
        SavedQuery savedQuery = new SavedQuery.Builder()
                .setTitle("title")
                .build();

        savedQuery = savedQueryService.create(savedQuery);
        assertNotNull(savedQuery.getId());
        assertThat(savedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(savedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(savedQuery.getDateCreated().isEqual(savedQuery.getDateModified().toInstant()));
        // Safe to assume completed in a day
        // TODO: mock out the datetime service used by spring auditing to check this properly
        assertTrue(savedQuery.getDateCreated().plusHours(1).isAfterNow());

        savedQuery.setQueryText("*");
        savedQuery = savedQueryService.update(savedQuery);
        assertThat(savedQuery.getDateCreated(), isA(DateTime.class));
        assertThat(savedQuery.getDateModified(), isA(DateTime.class));
        assertTrue(savedQuery.getDateModified().isAfter(savedQuery.getDateCreated().toInstant()));
    }

    @Test
    public void checkUserNotDuplicated() {
        final SavedQuery savedQuery1 = new SavedQuery.Builder()
                .setTitle("title1")
                .build();

        final SavedQuery savedQuery2 = new SavedQuery.Builder()
                .setTitle("title2")
                .build();

        savedQueryService.create(savedQuery1);
        savedQueryService.create(savedQuery2);

        assertNotNull(savedQuery1.getUser().getUserId());
        assertNotNull(savedQuery2.getUser().getUserId());
        assertEquals(savedQuery1.getUser().getUserId(), savedQuery2.getUser().getUserId());
    }
}
