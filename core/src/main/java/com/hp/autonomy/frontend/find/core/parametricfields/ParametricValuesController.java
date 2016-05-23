/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
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

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public abstract class ParametricValuesController<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    public static final String PARAMETRIC_VALUES_PATH = "/api/public/parametric";
    public static final String SECOND_PARAMETRIC_PATH = "second-parametric";

    public static final String QUERY_TEXT_PARAM = "queryText";
    public static final String FIELD_TEXT_PARAM = "fieldText";
    public static final String DATABASES_PARAM = "databases";
    public static final String MIN_DATE_PARAM = "minDate";
    public static final String MAX_DATE_PARAM = "maxDate";
    public static final String MIN_SCORE = "minScore";
    public static final String STATE_TOKEN_PARAM = "stateTokens";

    protected final ParametricValuesService<R, S, E> parametricValuesService;
    protected final QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    protected ParametricValuesController(final ParametricValuesService<R, S, E> parametricValuesService, final QueryRestrictionsBuilder<S> queryRestrictionsBuilder) {
        this.parametricValuesService = parametricValuesService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
    }

    protected abstract ParametricValues getParametricValuesInternal(
            String queryText,
            String fieldText,
            List<S> databases,
            DateTime minDate,
            DateTime maxDate,
            Integer minScore,
            List<String> stateTokens
    ) throws E;

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ParametricValues getParametricValues(
            @RequestParam(value = QUERY_TEXT_PARAM, defaultValue = "*") final String queryText,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(DATABASES_PARAM) final List<S> databases,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
            @RequestParam(value = MIN_SCORE, defaultValue = "0") final Integer minScore,
            @RequestParam(value = STATE_TOKEN_PARAM, required = false) final List<String> stateTokens
    ) throws E {
        return getParametricValuesInternal(queryText, fieldText, databases, minDate, maxDate, minScore, ensureList(stateTokens));
    }

    protected <T> List<T> ensureList(final List<T> maybeList) {
        return maybeList == null ? Collections.<T>emptyList() : maybeList;
    }
}
