package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshotController;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshotControllerTest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;

public class IdolSavedSnapshotControllerTest extends SavedSnapshotControllerTest<String, IdolSearchResult, AciErrorException> {
    @Override
    protected SavedSnapshotController<String, IdolSearchResult, AciErrorException> getControllerInstance() {
        return new IdolSavedSnapshotController(savedSnapshotService, documentsService);
    }
}
