/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
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
            final ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory,
            final FieldComparatorFactory fieldComparatorFactory
    ) {
        super(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory, fieldComparatorFactory);
    }
}
