/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@RequestMapping(SavedQueryController.PATH)
public abstract class SavedQueryController<S extends Serializable, D extends SearchResult, E extends Exception> {
    public static final String PATH = "/api/public/saved-query";
    static final String NEW_RESULTS_PATH = "/new-results/";

    private final SavedSearchService<SavedQuery> service;
    private final DocumentsService<S, D, E> documentsService;
    private final QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    protected SavedQueryController(final SavedSearchService<SavedQuery> service,
                                   final DocumentsService<S, D, E> documentsService,
                                   final QueryRestrictionsBuilder<S> queryRestrictionsBuilder) {
        this.service = service;
        this.documentsService = documentsService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedQuery> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedQuery create(
            @RequestBody final SavedQuery query
    ) {
        return service.create(query);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public SavedQuery update(
            @PathVariable("id") final long id,
            @RequestBody final SavedQuery query
    ) {
        return service.update(
                new SavedQuery.Builder(query).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }

    @RequestMapping(value = NEW_RESULTS_PATH + "{id}", method = RequestMethod.GET)
    public boolean checkForNewQueryResults(@SuppressWarnings("MVCPathVariableInspection") @PathVariable("id") final long id) throws E {
        boolean newResults = false;

        final SavedQuery savedQuery = service.get(id);
        final DateTime dateNewDocsLastFetched = savedQuery.getDateNewDocsLastFetched();
        if (savedQuery.getMaxDate() == null || savedQuery.getMaxDate().isAfter(dateNewDocsLastFetched)) {
            final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build(savedQuery.getQueryText(),
                    savedQuery.toFieldText(),
                    Collections.<S>emptyList(),
                    savedQuery.getDateNewDocsLastFetched(),
                    null,
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList());
            final SearchRequest<S> searchRequest = new SearchRequest<>(queryRestrictions, 1, 1, null, null, null, false, false, SearchRequest.QueryType.MODIFIED);
            final Documents<?> searchResults = documentsService.queryTextIndex(searchRequest);
            newResults = searchResults.getTotalResults() > 0;
        }

        return newResults;
    }
}
