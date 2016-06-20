/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
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

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
class HodParametricValuesController extends ParametricValuesController<HodQueryRestrictions, HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Autowired
    public HodParametricValuesController(final ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> parametricValuesService,
                                         final ObjectFactory<QueryRestrictions.Builder<HodQueryRestrictions, ResourceIdentifier>> queryRestrictionsBuilderFactory,
                                         final ObjectFactory<ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier>> parametricRequestBuilderFactory) {
        super(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(DATABASES_PARAM) final List<ResourceIdentifier> databases
    ) throws HodErrorException {
        final HodParametricRequest parametricRequest = buildRequest(fieldNames, databases, MAX_VALUES_DEFAULT, SortParam.DocumentCount);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @RequestMapping(value = BUCKET_PARAMETRIC_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<RangeInfo> getNumericParametricValuesInBuckets(
            @RequestParam(FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(DATABASES_PARAM) final List<ResourceIdentifier> databases,
            @RequestParam(TARGET_NUMBER_OF_BUCKETS_PARAM) final List<Integer> targetNumberOfBuckets,
            @RequestParam(value = BUCKET_MIN_PARAM, required = false) final List<Double> bucketMin,
            @RequestParam(value = BUCKET_MAX_PARAM, required = false) final List<Double> bucketMax
    ) throws HodErrorException {
        final HodParametricRequest parametricRequest = buildRequest(fieldNames, databases, null, SortParam.NumberIncreasing);
        return getNumericParametricValuesInBuckets(parametricRequest, targetNumberOfBuckets, bucketMin, bucketMax);
    }
}
