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

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.api.model.DependentParametricFieldsResponse;
import com.hp.autonomy.frontend.find.core.api.model.QueryTagInfoResponse;
import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.frontend.find.idol.api.ParametricApi;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.*;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
class IdolParametricValuesController extends ParametricValuesController<IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> implements ParametricApi {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolParametricValuesController(
            final IdolParametricValuesService parametricValuesService,
            final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
            final ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory,
            final FieldComparatorFactory fieldComparatorFactory
    ) {
        super(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory, fieldComparatorFactory);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<QueryTagInfoResponse> getParametricValues(
        final List<FieldPath> fieldNames,
        final List<String> databases,
        final Integer start,
        final Integer maxValues,
        final List<String> valueRestrictions,
        final String queryText,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens,
        final SortParam sort
    ) {
        return ResponseEntity.ok(super.getParametricValues(fieldNames, start, maxValues,
            valueRestrictions, queryText, fieldText, databases, minDate, maxDate, minScore,
            stateTokens, sort));
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<NumericRangeInfo> getNumericParametricBuckets(
        final FieldPath encodedField,
        final Integer targetNumberOfBuckets,
        final Double bucketMin,
        final Double bucketMax,
        final String queryText,
        final List<String> databases,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore
    ) {
        return ResponseEntity.ok(super.getNumericParametricValuesInBucketsForField(encodedField,
            targetNumberOfBuckets, bucketMin, bucketMax, queryText, fieldText, databases, minDate,
            maxDate, minScore));
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<DateRangeInfo> getDateParametricBuckets(
        final FieldPath encodedField,
        final Integer targetNumberOfBuckets,
        final ZonedDateTime bucketMin,
        final ZonedDateTime bucketMax,
        final String queryText,
        final List<String> databases,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore
    ) {
        return ResponseEntity.ok(super.getDateParametricValuesInBucketsForField(encodedField,
            targetNumberOfBuckets, bucketMin, bucketMax, queryText, fieldText, databases, minDate,
            maxDate, minScore));
    }

    @Override
    public ResponseEntity<NumericValueDetails> getNumericValueDetails(
        final FieldPath fieldName,
        final String queryText,
        final List<String> databases,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
    ) {
        return ResponseEntity.ok(super.getNumericValueDetails(fieldName, queryText, fieldText,
            databases, minDate, maxDate, minScore, stateTokens));
    }

    @Override
    public ResponseEntity<DateValueDetails> getDateValueDetails(
        final FieldPath fieldName,
        final String queryText,
        final List<String> databases,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
    ) {
        return ResponseEntity.ok(super.getDateValueDetails(fieldName, queryText, fieldText,
            databases, minDate, maxDate, minScore, stateTokens));
    }

    @Override
    public ResponseEntity<DependentParametricFieldsResponse> getDependentParametricValues(
        final List<FieldPath> fieldNames,
        final String queryText,
        final List<String> databases,
        final String fieldText,
        final ZonedDateTime minDate,
        final ZonedDateTime maxDate,
        final Integer minScore,
        final List<String> stateTokens
    ) {
        return ResponseEntity.ok(super.getDependentParametricValues(fieldNames, queryText,
            fieldText, databases, minDate, maxDate, minScore, stateTokens));
    }
}
