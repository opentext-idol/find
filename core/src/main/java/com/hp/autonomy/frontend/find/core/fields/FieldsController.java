/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping(FieldsController.FIELDS_PATH)
public abstract class FieldsController<R extends FieldsRequest, E extends Exception> {
    public static final String FIELDS_PATH = "/api/public/fields";
    public static final String GET_PARAMETRIC_FIELDS_PATH = "/parametric";
    static final String GET_PARAMETRIC_NUMERIC_FIELDS_PATH = "/parametric-numeric";
    static final String GET_PARAMETRIC_DATE_FIELDS_PATH = "/parametric-date";

    protected final FieldsService<R, E> fieldsService;

    protected FieldsController(final FieldsService<R, E> fieldsService) {
        this.fieldsService = fieldsService;
    }

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricFields(final R request) throws E {
        final Map<FieldTypeParam, List<TagName>> response = fieldsService.getFields(request, FieldTypeParam.Parametric, FieldTypeParam.Numeric, FieldTypeParam.NumericDate);
        final List<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.removeAll(response.get(FieldTypeParam.Numeric));
        parametricFields.removeAll(response.get(FieldTypeParam.NumericDate));
        return parametricFields;
    }

    @RequestMapping(value = GET_PARAMETRIC_NUMERIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricNumericFields(final R request) throws E {
        final Map<FieldTypeParam, List<TagName>> response = fieldsService.getFields(request, FieldTypeParam.Parametric, FieldTypeParam.Numeric);
        final List<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.retainAll(response.get(FieldTypeParam.Numeric));
        return parametricFields;
    }

    @RequestMapping(value = GET_PARAMETRIC_DATE_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricDateFields(final R request) throws E {
        final Map<FieldTypeParam, List<TagName>> response = fieldsService.getFields(request, FieldTypeParam.Parametric, FieldTypeParam.NumericDate);
        final List<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.retainAll(response.get(FieldTypeParam.NumericDate));
        parametricFields.add(new TagName(ParametricValuesService.AUTN_DATE_FIELD));
        return parametricFields;
    }
}
