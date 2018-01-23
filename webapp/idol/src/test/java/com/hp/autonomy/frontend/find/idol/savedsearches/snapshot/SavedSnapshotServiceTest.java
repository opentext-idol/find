/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchServiceTest;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;



@SpringBootTest(classes = SavedSnapshotService.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SavedSnapshotServiceTest extends AbstractSavedSearchServiceTest<SavedSnapshot, SavedSnapshot.Builder> {
    @SuppressWarnings("unused")
    @MockBean
    private SavedSnapshotRepository crudRepository;

    public SavedSnapshotServiceTest() {
        super(SavedSnapshot.Builder::new);
    }
}