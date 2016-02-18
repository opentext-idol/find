/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValues;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.fields.FieldType;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public class HodParametricValuesController extends ParametricValuesController<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    private final FieldsService<HodFieldsRequest, HodErrorException> fieldsService;

    @Autowired
    public HodParametricValuesController(final ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> parametricValuesService, final QueryRestrictionsBuilder<ResourceIdentifier> queryRestrictionsBuilder, FieldsService<HodFieldsRequest, HodErrorException> fieldsService) {
        super(parametricValuesService, queryRestrictionsBuilder);
        this.fieldsService = fieldsService;
    }

    @Override
    public ParametricValues getParametricValues(
            @RequestParam(QUERY_TEXT_PARAM) String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") String fieldText,
            @RequestParam(DATABASES_PARAM) List<ResourceIdentifier> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime maxDate) throws HodErrorException {
        final TagResponse tagResponse = fieldsService.getFields(new HodFieldsRequest.Builder().setDatabases(databases).build(), Arrays.asList(FieldType.parametric.name(), FieldType.numeric.name()));

        final List<String> parametricFields = tagResponse.getParametricTypeFields();

        final QueryRestrictions<ResourceIdentifier> queryRestrictions = queryRestrictionsBuilder.build(queryText, fieldText, databases, minDate, maxDate);

        final HodParametricRequest parametricRequest = buildParametricRequest(parametricFields, queryRestrictions);
        final Set<QueryTagInfo> parametricValues = parametricValuesService.getAllParametricValues(parametricRequest);

        final Set<QueryTagInfo> pureParametricValues = new LinkedHashSet<>();
        final Set<QueryTagInfo> numericParametricValues = new LinkedHashSet<>();

        final Set<String> numericFields = new HashSet<>(tagResponse.getNumericTypeFields());

        for (final QueryTagInfo queryTagInfo : parametricValues) {
            if (numericFields.contains(queryTagInfo.getName())) {
                numericParametricValues.add(queryTagInfo);
            }
            else {
                pureParametricValues.add(queryTagInfo);
            }
        }

        return new ParametricValues(pureParametricValues, numericParametricValues);
    }

    @Override
    protected HodParametricRequest buildParametricRequest(final List<String> fieldNames, final QueryRestrictions<ResourceIdentifier> queryRestrictions) {
        return new HodParametricRequest.Builder()
                .setFieldNames(fieldNames)
                .setQueryRestrictions(queryRestrictions)
                .build();
    }
}
