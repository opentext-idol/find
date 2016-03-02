package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
public abstract class SavedSnapshotController<S extends Serializable, R extends SearchResult, E extends Exception> {
    public static final String PATH = "/api/public/saved-snapshot";

    private static final Integer STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    protected final DocumentsService<S, R, E> documentsService;
    protected final SavedSearchService<SavedSnapshot> service;

    @Autowired
    public SavedSnapshotController(final SavedSnapshotService service, final DocumentsService<S, R, E> documentsService) {
        this.service = service;
        this.documentsService = documentsService;
    }

    protected abstract QueryRestrictions<S> buildStateTokenQueryRestrictions(final SavedSnapshot snapshot) throws E;

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot snapshot
    ) throws E {
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
    ) throws E {
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

    private StateTokenAndResultCount getStateTokenAndResultCount(final SavedSnapshot snapshot) throws E {
        return documentsService.getStateTokenAndResultCount(buildStateTokenQueryRestrictions(snapshot), STATE_TOKEN_MAX_RESULTS);
    }
}
