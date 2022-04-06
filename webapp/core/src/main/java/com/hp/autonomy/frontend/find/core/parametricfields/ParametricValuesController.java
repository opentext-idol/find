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

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.DependentParametricField;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.DateRangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.DateValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.NumericRangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.NumericValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_PATH)
public abstract class ParametricValuesController<Q extends QueryRestrictions<S>, R extends ParametricRequest<Q>, S extends Serializable, E extends Exception> {
    public static final String PARAMETRIC_PATH = "/api/public/parametric";
    public static final String DEPENDENT_VALUES_PATH = "/dependent-values";
    public static final String FIELD_NAMES_PARAM = "fieldNames";
    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String DATABASES_PARAM = "databases";
    static final String NUMERIC_PATH = "/numeric";
    static final String DATE_PATH = "/date";
    static final String VALUES_PATH = "/values";
    static final String BUCKET_PARAMETRIC_PATH = "/buckets";
    static final String TARGET_NUMBER_OF_BUCKETS_PARAM = "targetNumberOfBuckets";
    static final String BUCKET_MIN_PARAM = "bucketMin";
    static final String BUCKET_MAX_PARAM = "bucketMax";
    static final String VALUE_DETAILS_PATH = "/value-details";
    static final String FIELD_NAME_PARAM = "fieldName";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";
    private static final String MIN_SCORE = "minScore";
    private static final String STATE_TOKEN_PARAM = "stateTokens";
    private static final String MAX_VALUES_PARAM = "maxValues";
    private static final String VALUE_RESTRICTIONS_PARAM = "valueRestrictions";
    private static final String START_PARAM = "start";
    private static final String SORT_PARAM = "sort";

    private final ParametricValuesService<R, Q, E> parametricValuesService;
    private final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory;
    private final ObjectFactory<? extends ParametricRequestBuilder<R, Q, ?>> parametricRequestBuilderFactory;
    private final FieldComparatorFactory fieldComparatorFactory;

    protected ParametricValuesController(
        final ParametricValuesService<R, Q, E> parametricValuesService,
        final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory,
        final ObjectFactory<? extends ParametricRequestBuilder<R, Q, ?>> parametricRequestBuilderFactory,
        final FieldComparatorFactory fieldComparatorFactory
    ) {
        this.parametricValuesService = parametricValuesService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.parametricRequestBuilderFactory = parametricRequestBuilderFactory;
        this.fieldComparatorFactory = fieldComparatorFactory;
    }

    @RequestMapping(method = RequestMethod.GET, path = VALUES_PATH)
    @ResponseBody
    public List<QueryTagInfo> getParametricValues(
        @RequestParam(FIELD_NAMES_PARAM) final List<FieldPath> fieldNames,
        @RequestParam(value = START_PARAM, required = false) final Integer start,
        @RequestParam(value = MAX_VALUES_PARAM, required = false) final Integer maxValues,
        @RequestParam(value = VALUE_RESTRICTIONS_PARAM, required = false) final Collection<String> valueRestrictions,
        @RequestParam(value = QUERY_TEXT_PARAM, defaultValue = "*") final String queryText,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(DATABASES_PARAM) final Collection<S> databases,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
        @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens,
        @RequestParam(value = SORT_PARAM, defaultValue = "DocumentCount") final SortParam sort
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .stateMatchIds(ListUtils.emptyIfNull(stateTokens))
            .build();

        final ParametricRequestBuilder<R, Q, ?> builder = parametricRequestBuilderFactory.getObject()
            .fieldNames(ListUtils.emptyIfNull(fieldNames))
            .queryRestrictions(queryRestrictions)
            .sort(sort);

        // Don't override defaults set in the request builder
        if(start != null) {
            builder.start(start);
        }

        if(maxValues != null) {
            builder.maxValues(maxValues);
        }

        if(valueRestrictions != null) {
            builder.valueRestrictions(valueRestrictions);
        }

        return parametricValuesService.getParametricValues(builder.build()).stream()
            .sorted(fieldComparatorFactory.parametricFieldAndValuesComparator())
            .collect(Collectors.toList());
    }

    @RequestMapping(value = NUMERIC_PATH + BUCKET_PARAMETRIC_PATH + "/{encodedField}", method = RequestMethod.GET)
    @ResponseBody
    public NumericRangeInfo getNumericParametricValuesInBucketsForField(
            @PathVariable("encodedField") final FieldPath fieldName,
            @RequestParam(TARGET_NUMBER_OF_BUCKETS_PARAM) final Integer targetNumberOfBuckets,
            @RequestParam(BUCKET_MIN_PARAM) final Double bucketMin,
            @RequestParam(BUCKET_MAX_PARAM) final Double bucketMax,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final Collection<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .build();

        final R parametricRequest = parametricRequestBuilderFactory.getObject()
            .fieldName(fieldName)
            .maxValues(null)
            .queryRestrictions(queryRestrictions)
            .build();

        final BucketingParams<Double> bucketingParams = new BucketingParams<>(targetNumberOfBuckets, bucketMin, bucketMax);
        final Map<FieldPath, BucketingParams<Double>> bucketingParamsPerField = Collections.singletonMap(fieldName, bucketingParams);
        return parametricValuesService.getNumericParametricValuesInBuckets(parametricRequest, bucketingParamsPerField).get(0);
    }

    @RequestMapping(value = DATE_PATH + BUCKET_PARAMETRIC_PATH + "/{encodedField}", method = RequestMethod.GET)
    @ResponseBody
    public DateRangeInfo getDateParametricValuesInBucketsForField(
              @PathVariable("encodedField") final FieldPath fieldName,
              @RequestParam(TARGET_NUMBER_OF_BUCKETS_PARAM) final Integer targetNumberOfBuckets,
              @RequestParam(BUCKET_MIN_PARAM) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime bucketMin,
              @RequestParam(BUCKET_MAX_PARAM) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime bucketMax,
              @RequestParam(QUERY_TEXT_PARAM) final String queryText,
              @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
              @RequestParam(DATABASES_PARAM) final Collection<S> databases,
              @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
              @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
              @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .build();

        final R parametricRequest = parametricRequestBuilderFactory.getObject()
            .fieldName(fieldName)
            .maxValues(null)
            .queryRestrictions(queryRestrictions)
            .build();

        final BucketingParams<ZonedDateTime> bucketingParams = new BucketingParams<>(targetNumberOfBuckets, bucketMin, bucketMax);
        final Map<FieldPath, BucketingParams<ZonedDateTime>> bucketingParamsPerField = Collections.singletonMap(fieldName, bucketingParams);
        return parametricValuesService.getDateParametricValuesInBuckets(parametricRequest, bucketingParamsPerField).get(0);
    }

    @RequestMapping(method = RequestMethod.GET, value = NUMERIC_PATH + VALUE_DETAILS_PATH)
    @ResponseBody
    public NumericValueDetails getNumericValueDetails(
        @RequestParam(FIELD_NAME_PARAM) final FieldPath fieldName,
        @RequestParam(QUERY_TEXT_PARAM) final String queryText,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(DATABASES_PARAM) final Collection<S> databases,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
        @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .stateMatchIds(ListUtils.emptyIfNull(stateTokens))
            .build();

        final R parametricRequest = parametricRequestBuilderFactory.getObject()
            .fieldName(fieldName)
            .maxValues(null)
            .queryRestrictions(queryRestrictions)
            .build();

        final NumericValueDetails details =
            parametricValuesService.getNumericValueDetails(parametricRequest).get(fieldName);
        if (details == null) {
            throw new IllegalArgumentException(
                "Field " + fieldName.getFieldName() + " is not a parametric field");
        }
        return details;
    }

    @RequestMapping(method = RequestMethod.GET, value = DATE_PATH + VALUE_DETAILS_PATH)
    @ResponseBody
    public DateValueDetails getDateValueDetails(
        @RequestParam(FIELD_NAME_PARAM) final FieldPath fieldName,
        @RequestParam(QUERY_TEXT_PARAM) final String queryText,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(DATABASES_PARAM) final Collection<S> databases,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
        @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .stateMatchIds(ListUtils.emptyIfNull(stateTokens))
            .build();

        final R parametricRequest = parametricRequestBuilderFactory.getObject()
            .fieldName(fieldName)
            .maxValues(null)
            .queryRestrictions(queryRestrictions)
            .build();

        final DateValueDetails details =
            parametricValuesService.getDateValueDetails(parametricRequest).get(fieldName);
        if (details == null) {
            throw new IllegalArgumentException(
                "Field " + fieldName.getFieldName() + " is not a parametric field");
        }
        return details;
    }

    @RequestMapping(method = RequestMethod.GET, value = DEPENDENT_VALUES_PATH)
    @ResponseBody
    public List<DependentParametricField> getDependentParametricValues(
        @RequestParam(FIELD_NAMES_PARAM) final List<FieldPath> fieldNames,
        @RequestParam(QUERY_TEXT_PARAM) final String queryText,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(DATABASES_PARAM) final Collection<S> databases,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
        @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .queryText(queryText)
            .fieldText(fieldText)
            .databases(databases)
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .stateMatchIds(ListUtils.emptyIfNull(stateTokens))
            .build();

        final R parametricRequest = parametricRequestBuilderFactory.getObject()
            .fieldNames(ListUtils.emptyIfNull(fieldNames))
            .queryRestrictions(queryRestrictions)
            .maxValues(null)
            .build();

        return parametricValuesService.getDependentParametricValues(parametricRequest);
    }
}
