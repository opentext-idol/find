/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public abstract class ParametricValuesController<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    public static final String PARAMETRIC_VALUES_PATH = "/api/public/parametric";

    private final ParametricValuesService<R, S, E> parametricValuesService;

    protected ParametricValuesController(final ParametricValuesService<R, S, E> parametricValuesService) {
        this.parametricValuesService = parametricValuesService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<QueryTagInfo> getParametricValues(final R parametricRequest) throws E {
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }
}
