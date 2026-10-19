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

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.api.model.DependentParametricFieldsResponse;
import com.hp.autonomy.frontend.find.core.api.model.QueryTagInfoResponse;
import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.*;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ParametricValuesController<Q extends QueryRestrictions<S>, R extends ParametricRequest<Q>, S extends Serializable, E extends Exception> {
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

    protected QueryTagInfoResponse getParametricValues(
        final List<FieldPath> fieldNames,
        final Integer start,
        final Integer maxValues,
        final Collection<String> valueRestrictions,
        final String queryText,
        final String fieldText,
        final Collection<S> databases,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens,
        final SortParam sort
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

        return new QueryTagInfoResponse(parametricValuesService.getParametricValues(builder.build()).stream()
            .sorted(fieldComparatorFactory.parametricFieldAndValuesComparator())
            .collect(Collectors.toList()));
    }

    protected NumericRangeInfo getNumericParametricValuesInBucketsForField(
            final FieldPath fieldName,
            final Integer targetNumberOfBuckets,
            final Double bucketMin,
            final Double bucketMax,
            final String queryText,
            final String fieldText,
            final Collection<S> databases,
            final ZonedDateTime minDate,
            final ZonedDateTime maxDate,
            final Integer minScore
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

    protected DateRangeInfo getDateParametricValuesInBucketsForField(
              final FieldPath fieldName,
              final Integer targetNumberOfBuckets,
              final ZonedDateTime bucketMin,
              final ZonedDateTime bucketMax,
              final String queryText,
              final String fieldText,
              final Collection<S> databases,
              final ZonedDateTime minDate,
              final ZonedDateTime maxDate,
              final Integer minScore
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

    protected NumericValueDetails getNumericValueDetails(
        final FieldPath fieldName,
        final String queryText,
        final String fieldText,
        final Collection<S> databases,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
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

    protected DateValueDetails getDateValueDetails(
        final FieldPath fieldName,
        final String queryText,
        final String fieldText,
        final Collection<S> databases,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
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

    protected DependentParametricFieldsResponse getDependentParametricValues(
        final List<FieldPath> fieldNames,
        final String queryText,
        final String fieldText,
        final Collection<S> databases,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
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

        return new DependentParametricFieldsResponse(
            parametricValuesService.getDependentParametricValues(parametricRequest));
    }
}
