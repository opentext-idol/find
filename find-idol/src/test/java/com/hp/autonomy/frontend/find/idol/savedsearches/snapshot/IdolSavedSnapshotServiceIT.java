package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.AbstractSavedSnapshotServiceIT;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolSavedSnapshotServiceIT extends AbstractSavedSnapshotServiceIT<String, IdolSearchResult, AciErrorException> {
    protected void initController() {
        controller = new IdolSavedSnapshotController(savedSnapshotService, documentsService);
    }
}
