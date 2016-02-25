package com.hp.autonomy.frontend.find.hod.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshotController;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshotService;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(SavedSnapshotController.PATH)
public class HodSavedSnapshotController extends SavedSnapshotController<ResourceIdentifier, HodSearchResult, HodErrorException> {
    @Autowired
    public HodSavedSnapshotController(final SavedSnapshotService service, final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService) {
        super(service, documentsService);
    }

    private List<ResourceIdentifier> getDatabases(final Set<EmbeddableIndex> indexes) {
        final List<ResourceIdentifier> databases = new ArrayList<>();

        for(final EmbeddableIndex index: indexes) {
            databases.add(new ResourceIdentifier(index.getDomain(), index.getName()));
        }

        return databases;
    }

    @Override
    protected QueryRestrictions<ResourceIdentifier> buildStateTokenQueryRestrictions(final SavedSnapshot snapshot) throws HodErrorException {
        return new HodQueryRestrictions.Builder()
                .setDatabases(this.getDatabases(snapshot.getIndexes()))
                .setQueryText(snapshot.toQueryText())
                .setFieldText(snapshot.toFieldText())
                .setMaxDate(snapshot.getMaxDate())
                .setMinDate(snapshot.getMinDate())
                .build();
    }
}
