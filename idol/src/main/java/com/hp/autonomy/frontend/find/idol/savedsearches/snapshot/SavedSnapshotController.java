package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
public class SavedSnapshotController {
    public static final String PATH = "/api/public/saved-snapshot";

    private static final Integer STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    private final DocumentsService<String, IdolSearchResult, AciErrorException> documentsService;
    private final SavedSearchService<SavedSnapshot> service;

    @Autowired
    public SavedSnapshotController(final SavedSnapshotService service, final DocumentsService<String, IdolSearchResult, AciErrorException> documentsService) {
        this.service = service;
        this.documentsService = documentsService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot snapshot
    ) throws AciErrorException {
        final StateTokenAndResultCount stateTokenAndResultCount = getStateTokenAndResultCount(snapshot);

        return service.create(
                new SavedSnapshot.Builder(snapshot)
                        .setStateTokens(Collections.singletonList(stateTokenAndResultCount.getStateToken()))
                        .setResultCount(stateTokenAndResultCount.getResultCount())
                        .build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public SavedSnapshot update(
            @PathVariable("id") final long id,
            @RequestBody final SavedSnapshot snapshot
    ) throws AciErrorException {
        // It is only possible to update a snapshot's title
        return service.update(
                new SavedSnapshot.Builder()
                        .setId(id)
                        .setTitle(snapshot.getTitle())
                        .build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }

    private StateTokenAndResultCount getStateTokenAndResultCount(final SavedSnapshot snapshot) throws AciErrorException {
        final List<String> indexes;

        if (snapshot.getIndexes() == null) {
            indexes = null;
        } else {
            indexes = new ArrayList<>();

            for (final EmbeddableIndex embeddableIndex : snapshot.getIndexes()) {
                indexes.add(embeddableIndex.getName());
            }
        }

        final QueryRestrictions<String> restrictions = new IdolQueryRestrictions.Builder()
                .setAnyLanguage(true)
                .setDatabases(indexes)
                .setQueryText(snapshot.toQueryText())
                .setFieldText(snapshot.toFieldText())
                .setMaxDate(snapshot.getMaxDate())
                .setMinDate(snapshot.getMinDate())
                .setMinScore(snapshot.getMinScore())
                .build();

        return documentsService.getStateTokenAndResultCount(restrictions, STATE_TOKEN_MAX_RESULTS);
    }
}
