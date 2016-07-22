/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
class IdolFieldsController extends FieldsController<IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest> {
    @Autowired
    IdolFieldsController(
            final FieldsService<IdolFieldsRequest, AciErrorException> fieldsService,
            final ParametricValuesService<IdolParametricRequest, String, AciErrorException> parametricValuesService,
            final ObjectFactory<ParametricRequest.Builder<IdolParametricRequest, String>> parametricRequestBuilderFactory
    ) {
        super(fieldsService, parametricValuesService, parametricRequestBuilderFactory);
    }

    @Override
    protected IdolQueryRestrictions createValueDetailsQueryRestrictions(final IdolFieldsRequest request) {
        return new IdolQueryRestrictions.Builder()
                .setQueryText("*")
                .setAnyLanguage(true)
                .build();
    }
}
