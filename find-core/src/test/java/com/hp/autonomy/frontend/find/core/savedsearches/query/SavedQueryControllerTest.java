/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SavedQueryControllerTest {

    @Mock
    private SavedQueryService savedQueryService;

    private SavedQueryController savedQueryController;

    private final SavedQuery savedQuery = new SavedQuery.Builder()
            .setTitle("Any old saved search")
            .build();

    @Before
    public void setUp() {
        savedQueryController = new SavedQueryController(savedQueryService);
    }

    @Test
    public void create() {
        savedQueryController.create(savedQuery);
        verify(savedQueryService).create(Matchers.same(savedQuery));
    }

    @Test
    public void update() {
        when(savedQueryService.update(any(SavedQuery.class))).then(returnsFirstArg());

        final SavedQuery updatedQuery = savedQueryController.update(42, savedQuery);
        verify(savedQueryService).update(Matchers.isA(SavedQuery.class));
        assertEquals(updatedQuery.getId(), new Long(42L));
    }

    @Test
    public void getAll() {
        savedQueryController.getAll();
        verify(savedQueryService).getAll();
    }

    @Test
    public void delete() {
        savedQueryController.delete(42L);
        verify(savedQueryService).deleteById(eq(42L));
    }
}
