/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.*;

public abstract class AbstractSavedQueryServiceIT extends AbstractFindIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private SavedQueryService savedQueryService;

    @Test
    @DirtiesContext
    public void createFetchDelete() {
        final String title = "Any old saved search";
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setTitle(title)
                .build();

        final SavedQuery entity = savedQueryService.create(savedQuery);
        assertThat(entity.getId(), isA(Integer.class));
        assertNotNull(entity.getId());

        entity.setQueryText("*");
        final SavedQuery updatedEntity = savedQueryService.update(entity);
        assertEquals(updatedEntity.getQueryText(), "*");

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
}
