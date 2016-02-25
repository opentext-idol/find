/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@Slf4j
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public abstract class ParametricValuesController<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    public static final String PARAMETRIC_VALUES_PATH = "/api/public/parametric";
    public static final String SECOND_PARAMETRIC_PATH = "second-parametric";

    public static final String FIELD_NAMES_PARAM = "fieldNames";
    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String DATABASES_PARAM = "databases";
    public static final String MIN_DATE_PARAM = "minDate";
    public static final String MAX_DATE_PARAM = "maxDate";
    public static final String STATE_TOKEN_PARAM = "stateTokens";

    protected final ParametricValuesService<R, S, E> parametricValuesService;
    protected final QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    protected ParametricValuesController(final ParametricValuesService<R, S, E> parametricValuesService, final QueryRestrictionsBuilder<S> queryRestrictionsBuilder) {
        this.parametricValuesService = parametricValuesService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(
            @RequestParam(value = FIELD_NAMES_PARAM, required = false) final List<String> fieldNames,
            @RequestParam(QUERY_TEXT_PARAM) final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build(
                queryText,
                fieldText,
                databases,
                minDate,
                maxDate,
                stateTokens == null ? Collections.<String>emptyList() : stateTokens,
                Collections.<String>emptyList()
        );

        final R parametricRequest = buildParametricRequest(fieldNames == null ? Collections.<String>emptyList() : fieldNames, queryRestrictions);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

    protected abstract R buildParametricRequest(final List<String> fieldNames, final QueryRestrictions<S> queryRestrictions);
}
