/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@RequestMapping(SavedQueryController.PATH)
public abstract class SavedQueryController<RQ extends QueryRequest<Q>, S extends Serializable, Q extends QueryRestrictions<S>, D extends SearchResult, E extends Exception> {
    static final String PATH = "/api/bi/saved-query";
    static final String NEW_RESULTS_PATH = "/new-results/";

    protected final SavedSearchService<SavedQuery, SavedQuery.Builder> service;
    private final DocumentsService<RQ, ?, ?, Q, D, E> documentsService;
    private final FieldTextParser fieldTextParser;
    private final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory;
    private final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory;

    protected SavedQueryController(final SavedSearchService<SavedQuery, SavedQuery.Builder> service,
                                   final DocumentsService<RQ, ?, ?, Q, D, E> documentsService,
                                   final FieldTextParser fieldTextParser,
                                   final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory,
                                   final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory) {
        this.service = service;
        this.documentsService = documentsService;
        this.fieldTextParser = fieldTextParser;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
    }

    protected abstract S convertEmbeddableIndex(EmbeddableIndex embeddableIndex);

    protected abstract void addParams(QueryRequestBuilder<RQ, Q, ?> queryRequestBuilder);

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
    public void delete(@SuppressWarnings("MVCPathVariableInspection") @PathVariable("id") final long id) {
        service.deleteById(id);
    }

    @RequestMapping(value = NEW_RESULTS_PATH + "{id}", method = RequestMethod.GET)
    public int checkForNewQueryResults(@SuppressWarnings("MVCPathVariableInspection") @PathVariable("id") final long id) throws E {
        int newResults = 0;

        final SavedQuery savedQuery = service.get(id);
        final DateTime dateDocsLastFetched = savedQuery.getDateDocsLastFetched();
        if (savedQuery.getMaxDate() == null || savedQuery.getMaxDate().isAfter(dateDocsLastFetched)) {
            final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                    .queryText(savedQuery.toQueryText())
                    .fieldText(fieldTextParser.toFieldText(savedQuery))
                    .databases(convertEmbeddableIndexes(savedQuery.getIndexes()))
                    .minDate(dateDocsLastFetched)
                    .minScore(savedQuery.getMinScore())
                    .build();
            final QueryRequestBuilder<RQ, Q, ?> queryRequestBuilder = queryRequestBuilderFactory.getObject()
                    .queryRestrictions(queryRestrictions)
                    .maxResults(1001)
                    .queryType(QueryRequest.QueryType.MODIFIED);

            addParams(queryRequestBuilder);
            final RQ queryRequest = queryRequestBuilder.build();

            final Documents<?> searchResults = documentsService.queryTextIndex(queryRequest);
            newResults = searchResults.getTotalResults();
        }

        return newResults;
    }

    private Collection<S> convertEmbeddableIndexes(final Iterable<EmbeddableIndex> embeddableIndexes) {
        final Collection<S> indexes = new ArrayList<>(CollectionUtils.size(embeddableIndexes));
        if (embeddableIndexes != null) {
            for (final EmbeddableIndex embeddableIndex : embeddableIndexes) {
                indexes.add(convertEmbeddableIndex(embeddableIndex));
            }
        }

        return indexes;
    }
}
