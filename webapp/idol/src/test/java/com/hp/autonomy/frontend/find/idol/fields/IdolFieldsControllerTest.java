/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdolFieldsControllerTest extends AbstractFieldsControllerTest<IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest> {
    @Override
    protected FieldsController<IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest> constructController() {
        @SuppressWarnings("unchecked")
        final ObjectFactory<ParametricRequest.Builder<IdolParametricRequest, String>> requestBuilderFactory = mock(ObjectFactory.class);

        final ParametricRequest.Builder<IdolParametricRequest, String> builder = new IdolParametricRequest.Builder();
        when(requestBuilderFactory.getObject()).thenReturn(builder);

        return new IdolFieldsController(service, parametricValuesService, requestBuilderFactory);
    }

    @Override
    protected IdolFieldsRequest createRequest() {
        return new IdolFieldsRequest.Builder().build();
    }
}
