/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.RangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
class IdolParametricValuesController extends ParametricValuesController<IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @Autowired
    public IdolParametricValuesController(final ParametricValuesService<IdolParametricRequest, String, AciErrorException> parametricValuesService,
                                          final ObjectFactory<QueryRestrictions.Builder<IdolQueryRestrictions, String>> queryRestrictionsBuilderFactory,
                                          final ObjectFactory<ParametricRequest.Builder<IdolParametricRequest, String>> parametricRequestBuilderFactory) {
        super(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames
    ) throws AciErrorException {
        final IdolParametricRequest parametricRequest = buildRequest(fieldNames, MAX_VALUES_DEFAULT, SortParam.DocumentCount);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @RequestMapping(value = BUCKET_PARAMETRIC_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<RangeInfo> getNumericParametricValuesInBuckets(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(TARGET_NUMBER_OF_BUCKETS_PARAM) final List<Integer> targetNumberOfBuckets,
            @RequestParam(value = BUCKET_MIN_PARAM, required = false) final List<Double> bucketMin,
            @RequestParam(value = BUCKET_MAX_PARAM, required = false) final List<Double> bucketMax
    ) throws AciErrorException {
        final IdolParametricRequest parametricRequest = buildRequest(fieldNames, null, SortParam.NumberIncreasing);
        return getNumericParametricValuesInBuckets(parametricRequest, targetNumberOfBuckets, bucketMin, bucketMax);
    }

    private IdolParametricRequest buildRequest(final List<String> fieldNames, final Integer maxValues, final SortParam sort) {
        return buildRequest(fieldNames, Collections.<String>emptyList(), maxValues, sort);
    }
}
