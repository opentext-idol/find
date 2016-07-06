package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
@ConditionalOnProperty("hp.find.enableBi")
class SavedSnapshotController {
    static final String PATH = "/api/bi/saved-snapshot";

    private static final Integer STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    private final DocumentsService<String, IdolSearchResult, AciErrorException> documentsService;
    private final SavedSearchService<SavedSnapshot> service;
    private final FieldTextParser fieldTextParser;

    @Autowired
    public SavedSnapshotController(final DocumentsService<String, IdolSearchResult, AciErrorException> documentsService,
                                   final SavedSearchService<SavedSnapshot> service,
                                   final FieldTextParser fieldTextParser) {
        this.documentsService = documentsService;
        this.service = service;
        this.fieldTextParser = fieldTextParser;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot snapshot
    ) throws AciErrorException {
        final List<StateTokenAndResultCount> stateTokensList = Arrays.asList(
                getStateTokenAndResultCount(snapshot, false),
                getStateTokenAndResultCount(snapshot, true)
        );

        final Set<TypedStateToken> stateTokens = new HashSet<>();

        for (final StateTokenAndResultCount listToken : stateTokensList) {
            final TypedStateToken token = listToken.getTypedStateToken();
            stateTokens.add(token);
        }

        // use result count from the query without promotions
        final long resultCount = stateTokensList.get(0).getResultCount();

        return service.create(
                new SavedSnapshot.Builder(snapshot)
                        .setStateTokens(stateTokens)
                        .setResultCount(resultCount)
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

    private StateTokenAndResultCount getStateTokenAndResultCount(final SavedSnapshot snapshot, final boolean promotions) throws AciErrorException {
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
                .setFieldText(fieldTextParser.toFieldText(snapshot))
                .setMaxDate(snapshot.getMaxDate())
                .setMinDate(snapshot.getMinDate())
                .setMinScore(snapshot.getMinScore())
                .build();

        return documentsService.getStateTokenAndResultCount(restrictions, STATE_TOKEN_MAX_RESULTS, promotions);
    }
}
