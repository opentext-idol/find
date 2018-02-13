/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchServiceTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = SavedQueryService.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SavedQueryServiceTest extends AbstractSavedSearchServiceTest<SavedQuery, SavedQuery.Builder> {
    @SuppressWarnings("unused")
    @MockBean
    private SavedQueryRepository crudRepository;

    public SavedQueryServiceTest() {
        super(SavedQuery.Builder::new);
    }
}
