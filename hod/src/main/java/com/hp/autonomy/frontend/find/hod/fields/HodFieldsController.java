/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class HodFieldsController extends FieldsController<HodFieldsRequest, HodErrorException> {
    @Autowired
    public HodFieldsController(final FieldsService<HodFieldsRequest, HodErrorException> fieldsService) {
        super(fieldsService);
    }

    @Override
    public List<TagName> getParametricDateFields(final HodFieldsRequest request) throws HodErrorException {
        // TODO: Remove this override once FIND-180 is complete; we are just preventing AUTN_DATE from showing up in HoD as it will cause performance problems
        final Map<FieldTypeParam, List<TagName>> response = fieldsService.getFields(request, FieldTypeParam.Parametric, FieldTypeParam.NumericDate);
        final List<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.retainAll(response.get(FieldTypeParam.NumericDate));
        return parametricFields;
    }
}
