/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.*;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class RelatedConceptsController<T extends QuerySummaryElement, Q extends QueryRestrictions<S>, R extends RelatedConceptsRequest<Q>, S extends Serializable, E extends Exception> {
    private static final int QUERY_SUMMARY_LENGTH = 50;

    private final RelatedConceptsService<R, T, Q, E> relatedConceptsService;
    private final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory;
    private final ObjectFactory<? extends RelatedConceptsRequestBuilder<R, Q, ?>> relatedConceptsRequestBuilderFactory;

    protected RelatedConceptsController(final RelatedConceptsService<R, T, Q, E> relatedConceptsService,
                                        final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory,
                                        final ObjectFactory<? extends RelatedConceptsRequestBuilder<R, Q, ?>> relatedConceptsRequestBuilderFactory) {
        this.relatedConceptsService = relatedConceptsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.relatedConceptsRequestBuilderFactory = relatedConceptsRequestBuilderFactory;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    protected RelatedConceptsResponse<T> findRelatedConcepts(
        final String queryText,
        final String fieldText,
        final Collection<S> databases,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateMatchTokens,
        final List<String> stateDontMatchTokens,
        final Integer maxResults,
        final QueryRequest.QueryType queryType
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                .queryText(queryText)
                .fieldText(fieldText)
                .databases(databases)
                .minDate(minDate)
                .maxDate(maxDate)
                .minScore(minScore)
                .stateMatchIds(ListUtils.emptyIfNull(stateMatchTokens))
                .stateDontMatchIds(ListUtils.emptyIfNull(stateDontMatchTokens))
                .build();

        final R relatedConceptsRequest = relatedConceptsRequestBuilderFactory.getObject()
            .maxResults(maxResults)
            .querySummaryLength(QUERY_SUMMARY_LENGTH)
            .queryRestrictions(queryRestrictions)
            .queryType(queryType)
            .build();

        return new RelatedConceptsResponse<>(relatedConceptsService.findRelatedConcepts(relatedConceptsRequest));
    }
}
