/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilderFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.idol.RecursiveField;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.RangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public abstract class ParametricValuesController<Q extends QueryRestrictions<S>, R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    protected static final int MAX_VALUES_DEFAULT = 10;

    @SuppressWarnings("WeakerAccess")
    public static final String PARAMETRIC_VALUES_PATH = "/api/public/parametric";
    private static final String RESTRICTED_PARAMETRIC_VALUES_PATH = "/restricted";
    protected static final String BUCKET_PARAMETRIC_PATH = "/buckets";
    public static final String DEPENDENT_VALUES_PATH = "/dependent-values";

    public static final String FIELD_NAMES_PARAM = "fieldNames";
    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String DATABASES_PARAM = "databases";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";
    private static final String MIN_SCORE = "minScore";
    private static final String STATE_TOKEN_PARAM = "stateTokens";
    protected static final String TARGET_NUMBER_OF_BUCKETS_PARAM = "targetNumberOfBuckets";
    protected static final String BUCKET_MIN_PARAM = "bucketMin";
    protected static final String BUCKET_MAX_PARAM = "bucketMax";

    protected final ParametricValuesService<R, S, E> parametricValuesService;
    protected final QueryRestrictionsBuilderFactory<Q, S> queryRestrictionsBuilderFactory;
    private final ObjectFactory<ParametricRequest.Builder<R, S>> parametricRequestBuilderFactory;

    protected ParametricValuesController(final ParametricValuesService<R, S, E> parametricValuesService,
                                         final QueryRestrictionsBuilderFactory<Q, S> queryRestrictionsBuilderFactory,
                                         final ObjectFactory<ParametricRequest.Builder<R, S>> parametricRequestBuilderFactory) {
        this.parametricValuesService = parametricValuesService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.parametricRequestBuilderFactory = parametricRequestBuilderFactory;
    }

    protected List<RangeInfo> getNumericParametricValuesInBuckets(final R parametricRequest,
                                                                  final List<Integer> targetNumberOfBuckets,
                                                                  final List<Double> bucketMin,
                                                                  final List<Double> bucketMax
    ) throws E {
        final List<String> fieldNames = parametricRequest.getFieldNames();
        final int numberOfFields = fieldNames.size();

        // A null value for a boundary list parameter means that no fields have that boundary
        if (numberOfFields != targetNumberOfBuckets.size() || bucketMin != null && numberOfFields != bucketMin.size() || bucketMax != null && numberOfFields != bucketMax.size()) {
            throw new IllegalArgumentException("Invalid bucketing parameters. Bucket boundaries must be supplied for every field or no fields.");
        }

        final Map<String, BucketingParams> bucketingParamsPerField = new LinkedHashMap<>(numberOfFields);
        final Iterator<String> fieldNameIterator = fieldNames.iterator();
        final Iterator<Integer> targetNumberOfBucketsIterator = targetNumberOfBuckets.iterator();

        final Iterator<Double> bucketMinIterator = bucketMin == null ? null : bucketMin.iterator();
        final Iterator<Double> bucketMaxIterator = bucketMax == null ? null : bucketMax.iterator();

        while (fieldNameIterator.hasNext()) {
            final String fieldName = fieldNameIterator.next();
            final Double min = bucketMinIterator == null ? null : bucketMinIterator.next();
            final Double max = bucketMaxIterator == null ? null : bucketMaxIterator.next();
            bucketingParamsPerField.put(fieldName, new BucketingParams(targetNumberOfBucketsIterator.next(), min, max));
        }

        return parametricValuesService.getNumericParametricValuesInBuckets(parametricRequest, bucketingParamsPerField);
    }

    @RequestMapping(method = RequestMethod.GET, path = RESTRICTED_PARAMETRIC_VALUES_PATH)
    @ResponseBody
    public Set<QueryTagInfo> getRestrictedParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(value = QUERY_TEXT_PARAM, defaultValue = "*") final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final R parametricRequest = buildRequest(fieldNames, queryText, fieldText, databases, minDate, maxDate, minScore, stateTokens, MAX_VALUES_DEFAULT, SortParam.DocumentCount);
        return parametricValuesService.getAllParametricValues(parametricRequest);
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
        final R parametricRequest = buildRequest(fieldNames, queryText, fieldText, databases, minDate, maxDate, minScore, stateTokens, null, null);
        return parametricValuesService.getDependentParametricValues(parametricRequest);
    }

    protected R buildRequest(final List<String> fieldNames, final List<S> databases, final Integer maxValues, final SortParam sort) {
        return buildRequest(fieldNames, "*", null, databases, null, null, null, null, maxValues, sort);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private R buildRequest(final List<String> fieldNames, final String queryText, final String fieldText, final List<S> databases, final DateTime minDate, final DateTime maxDate, final Integer minScore, final List<String> stateTokens, final Integer maxValues, final SortParam sort) {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilderFactory.createBuilder()
                .setQueryText(queryText)
                .setFieldText(fieldText)
                .setDatabases(databases)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setMinScore(minScore)
                .setStateMatchId(ListUtils.emptyIfNull(stateTokens))
                .build();
        return parametricRequestBuilderFactory.getObject()
                .setFieldNames(ListUtils.emptyIfNull(fieldNames))
                .setQueryRestrictions(queryRestrictions)
                .setMaxValues(maxValues)
                .setSort(sort)
                .build();
    }
}
