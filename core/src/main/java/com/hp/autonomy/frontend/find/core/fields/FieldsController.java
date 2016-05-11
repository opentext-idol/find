/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableList;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.types.requests.idol.actions.tags.TagResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@RequestMapping(FieldsController.FIELDS_PATH)
public abstract class FieldsController<R extends FieldsRequest, E extends Exception> {
    static final String FIELDS_PATH = "/api/public/fields";
    static final String GET_PARAMETRIC_FIELDS_PATH = "/parametric";
    static final String GET_NUMERIC_FIELDS_PATH = "/numeric";

    private final FieldsService<R, E> fieldsService;

    protected FieldsController(final FieldsService<R, E> fieldsService) {
        this.fieldsService = fieldsService;
    }

    protected abstract String getParametricType();

    protected abstract String getNumericType();

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<String> getParametricFields(final R request) throws E {
        final TagResponse response = fieldsService.getFields(request, ImmutableList.of(getParametricType(), getNumericType()));
        final List<String> parametricFields = new ArrayList<>(response.getParametricTypeFields());
        parametricFields.removeAll(response.getNumericTypeFields());
        return parametricFields;
    }

    @RequestMapping(value = GET_NUMERIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<String> getParametricNumericFields(final R request) throws E {
        final TagResponse response = fieldsService.getFields(request, ImmutableList.of(getParametricType(), getNumericType()));
        final List<String> parametricFields = new ArrayList<>(response.getParametricTypeFields());
        parametricFields.retainAll(response.getNumericTypeFields());
        return parametricFields;
    }
}
