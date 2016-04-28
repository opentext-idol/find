/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValues;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.types.idol.RecursiveField;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
public class IdolParametricValuesController extends ParametricValuesController<IdolParametricRequest, String, AciErrorException> {
    public static final String FIELD_NAMES_PARAM = "fieldNames";

    @Autowired
    public IdolParametricValuesController(final ParametricValuesService<IdolParametricRequest, String, AciErrorException> parametricValuesService, final QueryRestrictionsBuilder<String> queryRestrictionsBuilder) {
        super(parametricValuesService, queryRestrictionsBuilder);
    }

    @Override
    public ParametricValues getParametricValuesInternal(
            final String queryText,
            final String fieldText,
            final List<String> databases,
            final DateTime minDate,
            final DateTime maxDate,
            final Integer minScore,
            final List<String> stateTokens
    ) throws AciErrorException {
        final QueryRestrictions<String> queryRestrictions = queryRestrictionsBuilder.build(
                queryText,
                fieldText,
                databases,
                minDate,
                maxDate,
                minScore,
                stateTokens,
                Collections.<String>emptyList());

        final IdolParametricRequest parametricRequest = new IdolParametricRequest.Builder()
                .setFieldNames(Collections.<String>emptyList())
                .setQueryRestrictions(queryRestrictions)
                .build();

        final Set<QueryTagInfo> allParametricValues = parametricValuesService.getAllParametricValues(parametricRequest);
        return new ParametricValues(allParametricValues, null);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET, value = SECOND_PARAMETRIC_PATH)
    @ResponseBody
    public List<RecursiveField> getSecondParametricValues(
            @RequestParam(value = FIELD_NAMES_PARAM) final List<String> fieldNames,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<String> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws AciErrorException, InterruptedException {
        final QueryRestrictions<String> queryRestrictions = queryRestrictionsBuilder.build(
                queryText,
                fieldText,
                databases,
                minDate,
                maxDate,
                minScore,
                ensureList(stateTokens),
                Collections.<String>emptyList()
        );

        final IdolParametricRequest parametricRequest = new IdolParametricRequest.Builder()
                .setFieldNames(ensureList(fieldNames))
                .setQueryRestrictions(queryRestrictions)
                .build();

        return parametricValuesService.getDependentParametricValues(parametricRequest);
    }
}
