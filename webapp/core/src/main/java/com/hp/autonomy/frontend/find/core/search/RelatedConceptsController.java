/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.fields.FieldAndValue;
import com.hp.autonomy.frontend.find.core.fields.ParametricRange;
import com.hp.autonomy.frontend.find.core.fieldtext.FieldTextParser;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.apache.commons.collections4.ListUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Controller
@SuppressWarnings("SpringJavaAutowiringInspection")
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
public abstract class RelatedConceptsController<Q extends QuerySummaryElement, R extends QueryRestrictions<S>, L extends RelatedConceptsRequest<S>, S extends Serializable, E extends Exception> {
    public static final String RELATED_CONCEPTS_PATH = "/api/public/search/find-related-concepts";

    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String DATABASES_PARAM = "databases";
    public static final String STATE_TOKEN_PARAM = "stateTokens";
    private static final String FIELD_MATCH_PARAM = "field_matches";
    private static final String FIELD_RANGE_PARAM = "field_ranges";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";
    private static final String MIN_SCORE_PARAM = "minScore";
    private static final String MAX_RESULTS = "maxResults";

    private static final int QUERY_SUMMARY_LENGTH = 50;

    private final RelatedConceptsService<Q, S, E> relatedConceptsService;
    private final QueryRestrictionsBuilderFactory<R, S> queryRestrictionsBuilderFactory;
    private final ObjectFactory<RelatedConceptsRequest.Builder<L, S>> relatedConceptsRequestBuilderFactory;
    private final FieldTextParser fieldTextParser;

    protected RelatedConceptsController(final RelatedConceptsService<Q, S, E> relatedConceptsService,
                                        final QueryRestrictionsBuilderFactory<R, S> queryRestrictionsBuilderFactory,
                                        final ObjectFactory<RelatedConceptsRequest.Builder<L, S>> relatedConceptsRequestBuilderFactory,
                                        final FieldTextParser fieldTextParser) {
        this.relatedConceptsService = relatedConceptsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.relatedConceptsRequestBuilderFactory = relatedConceptsRequestBuilderFactory;
        this.fieldTextParser = fieldTextParser;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Q> findRelatedConcepts(
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_MATCH_PARAM, required = false) final Collection<FieldAndValue> fieldAndValues,
            @RequestParam(value = FIELD_RANGE_PARAM, required = false) final Collection<ParametricRange> parametricRanges,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE_PARAM, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens,
            @RequestParam(value = MAX_RESULTS, required = false) final Integer maxResults
    ) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilderFactory.createBuilder()
                .setQueryText(queryText)
                .setFieldText(fieldTextParser.toFieldText(fieldAndValues, parametricRanges, null))
                .setDatabases(databases)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setMinScore(minScore)
                .setStateMatchId(ListUtils.emptyIfNull(stateTokens))
                .build();

        final RelatedConceptsRequest<S> relatedConceptsRequest = relatedConceptsRequestBuilderFactory.getObject()
                .setMaxResults(maxResults)
                .setQuerySummaryLength(QUERY_SUMMARY_LENGTH)
                .setQueryRestrictions(queryRestrictions)
                .build();
        return relatedConceptsService.findRelatedConcepts(relatedConceptsRequest);
    }
}
