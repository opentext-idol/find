/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Set;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/api/public/parametric")
public class ParametricValuesController<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {

    @Autowired
    private ParametricValuesService<R, S, E> parametricValuesService;

    @Autowired
    private ParametricRequestBuilder<R, S> parametricRequestBuilder;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(
            @RequestParam("databases") final Set<S> databases,
            @RequestParam(value = "fieldNames", required = false) final Set<String> fieldNames,
            @RequestParam("queryText") final String queryText,
            @RequestParam("fieldText") final String fieldText
    ) throws E {
        final R parametricRequest = parametricRequestBuilder.buildRequest(databases, fieldNames, queryText, fieldText);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }
}
