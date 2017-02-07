/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_PATH)
class IdolParametricValuesController extends ParametricValuesController<IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolParametricValuesController(
            final IdolParametricValuesService parametricValuesService,
            final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
            final ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory
    ) {
        super(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }
}
