/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.idol.RecursiveField;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.apache.commons.collections4.ListUtils;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public abstract class ParametricValuesController<Q extends QueryRestrictions<S>, R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    @SuppressWarnings("WeakerAccess")
    public static final String PARAMETRIC_VALUES_PATH = "/api/public/parametric";
    static final String NUMERIC_PARAMETRIC_PATH = "/numeric";
    public static final String DEPENDENT_VALUES_PATH = "/dependent-values";

    public static final String FIELD_NAMES_PARAM = "fieldNames";
    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String DATABASES_PARAM = "databases";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";
    private static final String MIN_SCORE = "minScore";
    private static final String STATE_TOKEN_PARAM = "stateTokens";

    private final ParametricValuesService<R, S, E> parametricValuesService;
    protected final QueryRestrictions.Builder<Q, S> queryRestrictionsBuilder;
    private final ParametricRequest.Builder<R, S> parametricRequestBuilder;

    protected ParametricValuesController(final ParametricValuesService<R, S, E> parametricValuesService,
                                         final QueryRestrictions.Builder<Q, S> queryRestrictionsBuilder,
                                         final ParametricRequest.Builder<R, S> parametricRequestBuilder) {
        this.parametricValuesService = parametricValuesService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
        this.parametricRequestBuilder = parametricRequestBuilder;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final R parametricRequest = buildRequest(fieldNames, queryText, fieldText, databases, minDate, maxDate, minScore, stateTokens);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = NUMERIC_PARAMETRIC_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getNumericParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final R parametricRequest = buildRequest(fieldNames, queryText, fieldText, databases, minDate, maxDate, minScore, stateTokens);
        return parametricValuesService.getNumericParametricValues(parametricRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET, value = DEPENDENT_VALUES_PATH)
    @ResponseBody
    public List<RecursiveField> getDependentParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final R parametricRequest = buildRequest(fieldNames, queryText, fieldText, databases, minDate, maxDate, minScore, stateTokens);
        return parametricValuesService.getDependentParametricValues(parametricRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private R buildRequest(final List<String> fieldNames, final String queryText, final String fieldText, final List<S> databases, final DateTime minDate, final DateTime maxDate, final Integer minScore, final List<String> stateTokens) {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder
                .setQueryText(queryText)
                .setFieldText(fieldText)
                .setDatabases(databases)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setMinScore(minScore)
                .setStateMatchId(ListUtils.emptyIfNull(stateTokens))
                .build();
        return parametricRequestBuilder
                .setFieldNames(ListUtils.emptyIfNull(fieldNames))
                .setQueryRestrictions(queryRestrictions)
                .build();
    }
}
