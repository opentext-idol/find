package com.hp.autonomy.frontend.find.hod.savedsearches;


import com.hp.autonomy.frontend.find.core.savedsearches.SavedSnapshotControllerTest;
import com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot.SavedSnapshotController;
import com.hp.autonomy.frontend.find.hod.savedsearches.savedsnapshot.HodSavedSnapshotController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;

public class HodSavedSnapshotControllerTest extends SavedSnapshotControllerTest<ResourceIdentifier, HodSearchResult, HodErrorException> {
    @Override
    protected SavedSnapshotController<ResourceIdentifier, HodSearchResult, HodErrorException> getControllerInstance() {
        return new HodSavedSnapshotController(savedSnapshotService, documentsService);
    }
}
