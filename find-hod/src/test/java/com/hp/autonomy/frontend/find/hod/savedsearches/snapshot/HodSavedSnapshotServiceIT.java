package com.hp.autonomy.frontend.find.hod.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.AbstractSavedSnapshotServiceIT;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.junit.Ignore;
import org.springframework.boot.test.SpringApplicationConfiguration;

@Ignore //TODO
@SpringApplicationConfiguration(classes = HodFindApplication.class)
public class HodSavedSnapshotServiceIT extends AbstractSavedSnapshotServiceIT<ResourceIdentifier, HodSearchResult, HodErrorException> {
    protected void initController() {
        controller = new HodSavedSnapshotController(savedSnapshotService, documentsService);
    }
}
