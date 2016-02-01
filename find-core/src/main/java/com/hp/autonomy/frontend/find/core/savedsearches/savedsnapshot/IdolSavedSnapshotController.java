package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(SavedSnapshotController.PATH)
public class IdolSavedSnapshotController extends SavedSnapshotController {
    public IdolSavedSnapshotController(SavedSnapshotService service, DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService) {
        super(service, documentsService);
    }

    private List<String> getDatabases(Set<EmbeddableIndex> indexes) {
        List<String> databases = new ArrayList<>();

        for(EmbeddableIndex index: indexes) {
            databases.add(index.getName());
        }

        return databases;
    }

    @Override
    protected String getStateToken(final SavedSnapshot snapshot) throws Exception {
        IdolQueryRestrictions.Builder queryRestrictionsBuilder = new IdolQueryRestrictions.Builder()
                .setDatabases(this.getDatabases(snapshot.getIndexes()))
                .setQueryText(this.getQueryText(snapshot)).setFieldText(this.getFieldText(snapshot.getParametricValues()))
                .setMaxDate(snapshot.getMaxDate())
                .setMinDate(snapshot.getMinDate());

        return documentsService.getStateToken(queryRestrictionsBuilder.build(), Integer.MAX_VALUE);
    }
}
