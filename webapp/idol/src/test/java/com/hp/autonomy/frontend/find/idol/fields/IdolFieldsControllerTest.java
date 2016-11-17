/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.springframework.beans.factory.ObjectFactory;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdolFieldsControllerTest extends AbstractFieldsControllerTest<IdolFieldsController, IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest> {
    @Override
    protected IdolFieldsController constructController() {
        @SuppressWarnings("unchecked")
        final ObjectFactory<ParametricRequest.ParametricRequestBuilder<IdolParametricRequest, String>> requestBuilderFactory = mock(ObjectFactory.class);

        final ParametricRequest.ParametricRequestBuilder<IdolParametricRequest, String> builder = IdolParametricRequest.builder();
        when(requestBuilderFactory.getObject()).thenReturn(builder);

        return new IdolFieldsController(service, parametricValuesService, requestBuilderFactory, configService);
    }

    @Override
    protected List<TagName> getParametricFields() {
        return controller.getParametricFields();
    }

    @Override
    protected List<FieldAndValueDetails> getParametricDateFields() {
        return controller.getParametricDateFields();
    }

    @Override
    protected List<FieldAndValueDetails> getParametricNumericFields() {
        return controller.getParametricNumericFields();
    }
}
