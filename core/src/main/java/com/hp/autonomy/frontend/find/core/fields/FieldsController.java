/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping(FieldsController.FIELDS_PATH)
public abstract class FieldsController<R extends FieldsRequest, E extends Exception> {
    static final String FIELDS_PATH = "/api/public/fields";
    static final String GET_PARAMETRIC_FIELDS_PATH = "/parametric";

    private final FieldsService<R, E> fieldsService;

    protected FieldsController(final FieldsService<R, E> fieldsService) {
        this.fieldsService = fieldsService;
    }

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<String> getParametricFields(final R request) throws E {
        return fieldsService.getParametricFields(request);
    }
}
