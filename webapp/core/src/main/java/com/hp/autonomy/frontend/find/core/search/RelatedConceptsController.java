/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.*;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Controller
@SuppressWarnings("SpringJavaAutowiringInspection")
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
public abstract class RelatedConceptsController<T extends QuerySummaryElement, Q extends QueryRestrictions<S>, R extends RelatedConceptsRequest<Q>, S extends Serializable, E extends Exception> {
    public static final String RELATED_CONCEPTS_PATH = "/api/public/search/find-related-concepts";

    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String DATABASES_PARAM = "databases";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String STATE_MATCH_TOKEN_PARAM = "stateMatchTokens";
    private static final String STATE_DONT_MATCH_TOKEN_PARAM = "stateDontMatchTokens";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";
    private static final String MIN_SCORE_PARAM = "minScore";
    private static final String MAX_RESULTS = "maxResults";
    private static final String QUERY_TYPE_PARAM = "queryType";

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
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<T> findRelatedConcepts(
        @RequestParam(QUERY_TEXT_PARAM) final String queryText,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(DATABASES_PARAM) final Collection<S> databases,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = MIN_SCORE_PARAM, defaultValue = "0") final Integer minScore,
        @RequestParam(value = STATE_MATCH_TOKEN_PARAM, required = false) final List<String> stateMatchTokens,
        @RequestParam(value = STATE_DONT_MATCH_TOKEN_PARAM, required = false) final List<String> stateDontMatchTokens,
        @RequestParam(value = MAX_RESULTS, required = false) final Integer maxResults,
        @RequestParam(value = QUERY_TYPE_PARAM, defaultValue = "MODIFIED") final String queryType
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
            .queryType(QueryRequest.QueryType.valueOf(queryType))
            .build();

        return relatedConceptsService.findRelatedConcepts(relatedConceptsRequest);
    }
}
