/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SavedSnapshotControllerTest {
    @Mock
    private SavedSnapshotService savedSnapshotService;

    @Mock
    private DocumentsService<String, IdolSearchResult, AciErrorException> documentsService;

    @Mock
    private FieldTextParser fieldTextParser;

    @Mock
    private TypedStateToken stateToken;

    private SavedSnapshotController savedSnapshotController;

    private final SavedSnapshot savedSnapshot = new SavedSnapshot.Builder()
            .setTitle("Any old saved search")
            .setIndexes(Collections.singleton(new EmbeddableIndex("index", "domain")))
            .build();

    @Before
    public void setUp() {
        savedSnapshotController = new SavedSnapshotController(documentsService, savedSnapshotService, fieldTextParser);
    }

    @Test
    public void create() throws Exception {
        final StateTokenAndResultCount stateTokenAndResultCount = new StateTokenAndResultCount(stateToken, 123);
        when(documentsService.getStateTokenAndResultCount(Matchers.<QueryRestrictions<String>>any(), any(Integer.class), any(Boolean.class))).thenReturn(stateTokenAndResultCount);

        savedSnapshotController.create(savedSnapshot);
        verify(savedSnapshotService).create(any(SavedSnapshot.class));
    }

    @Test
    public void update() throws AciErrorException {
        when(savedSnapshotService.update(any(SavedSnapshot.class))).then(returnsFirstArg());

        final SavedSnapshot updatedQuery = savedSnapshotController.update(42, savedSnapshot);
        verify(savedSnapshotService).update(Matchers.isA(SavedSnapshot.class));
        assertEquals(new Long(42L), updatedQuery.getId());
    }

    @Test
    public void getAll() {
        savedSnapshotController.getAll();
        verify(savedSnapshotService).getAll();
    }

    @Test
    public void delete() {
        savedSnapshotController.delete(42L);
        verify(savedSnapshotService).deleteById(eq(42L));
    }
}
