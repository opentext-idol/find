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
import com.hp.autonomy.types.requests.Documents;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

public abstract class DocumentsController<RQ extends QueryRequest<Q>, RS extends SuggestRequest<Q>, RC extends GetContentRequest<T>, S extends Serializable, Q extends QueryRestrictions<S>, T extends GetContentRequestIndex<S>, R extends SearchResult, E extends Exception> {
    protected final DocumentsService<RQ, RS, RC, Q, R, E> documentsService;
    protected final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory;
    protected final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory;
    private final ObjectFactory<? extends SuggestRequestBuilder<RS, Q, ?>> suggestRequestBuilderFactory;
    private final ObjectFactory<? extends GetContentRequestBuilder<RC, T, ?>> getContentRequestBuilderFactory;
    private final ObjectFactory<? extends GetContentRequestIndexBuilder<T, S, ?>> getContentRequestIndexBuilderFactory;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    protected DocumentsController(final DocumentsService<RQ, RS, RC, Q, R, E> documentsService,
                                  final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory,
                                  final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory,
                                  final ObjectFactory<? extends SuggestRequestBuilder<RS, Q, ?>> suggestRequestBuilderFactory,
                                  final ObjectFactory<? extends GetContentRequestBuilder<RC, T, ?>> getContentRequestBuilderFactory,
                                  final ObjectFactory<? extends GetContentRequestIndexBuilder<T, S, ?>> getContentRequestIndexBuilderFactory) {
        this.documentsService = documentsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
        this.suggestRequestBuilderFactory = suggestRequestBuilderFactory;
        this.getContentRequestBuilderFactory = getContentRequestBuilderFactory;
        this.getContentRequestIndexBuilderFactory = getContentRequestIndexBuilderFactory;
    }

    protected abstract <EX> EX throwException(final String message) throws E;

    protected abstract void addParams(final GetContentRequestBuilder<RC, T, ?> request);

    protected Integer getMaxSummaryCharacters() {
        return 250;
    }

    protected abstract String getReferenceField();

    @SuppressWarnings("MethodWithTooManyParameters")
    protected Documents<R> query(
        final String queryText,
        final int resultsStart,
        final int maxResults,
        final String summary,
        final List<S> databases,
        final String fieldText,
        final String sort,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final boolean highlight,
        final int minScore,
        final boolean autoCorrect,
        final boolean intentBasedRanking,
        final QueryRequest.QueryType queryType
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(ListUtils.emptyIfNull(databases))
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .build();

        final RQ queryRequest = queryRequestBuilderFactory.getObject()
            .queryRestrictions(queryRestrictions)
            .start(resultsStart)
            .maxResults(maxResults)
            .summaryCharacters(getMaxSummaryCharacters())
            .highlight(highlight)
            .autoCorrect(autoCorrect)
            .summary(summary)
            .sort(sort)
            .queryType(queryType)
            .intentBasedRanking(intentBasedRanking)
            .referenceField(getReferenceField())
            .build();

        return documentsService.queryTextIndex(queryRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    protected Documents<R> findSimilar(
        final String reference,
        final int resultsStart,
        final int maxResults,
        final String summary,
        final List<S> databases,
        final String fieldText,
        final String sort,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final boolean highlight,
        final int minScore
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .fieldText(fieldText)
            .databases(ListUtils.emptyIfNull(databases))
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .build();

        final RS suggestRequest = suggestRequestBuilderFactory.getObject()
            .reference(reference)
            .queryRestrictions(queryRestrictions)
            .start(resultsStart)
            .maxResults(maxResults)
            .summaryCharacters(getMaxSummaryCharacters())
            .highlight(highlight)
            .summary(summary)
            .sort(sort)
            .referenceField(getReferenceField())
            .build();

        return documentsService.findSimilar(suggestRequest);
    }

    protected R fetchDocumentContent(
        final String reference,
        final S database
    ) throws E {
        final T getContentRequestIndex = getContentRequestIndexBuilderFactory.getObject()
            .index(database)
            .reference(reference)
            .build();
        final GetContentRequestBuilder<RC, T, ?> requestBuilder = getContentRequestBuilderFactory.getObject()
            .indexAndReferences(getContentRequestIndex)
            .referenceField(getReferenceField());
        addParams(requestBuilder);
        final RC getContentRequest = requestBuilder.build();

        final List<R> results = documentsService.getDocumentContent(getContentRequest);
        return results.isEmpty()
            ? throwException("No content found for document with reference " + reference + " in database " + database)
            : results.get(0);
    }
}
