/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HodFieldsControllerTest extends AbstractFieldsControllerTest<HodFieldsRequest, HodErrorException, ResourceIdentifier, HodQueryRestrictions, HodParametricRequest> {
    @Override
    protected FieldsController<HodFieldsRequest, HodErrorException, ResourceIdentifier, HodQueryRestrictions, HodParametricRequest> constructController() {
        @SuppressWarnings("unchecked")
        final ObjectFactory<ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier>> requestBuilderFactory = mock(ObjectFactory.class);

        final ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier> builder = new HodParametricRequest.Builder();
        when(requestBuilderFactory.getObject()).thenReturn(builder);

        return new HodFieldsController(service, parametricValuesService, requestBuilderFactory);
    }

    @Override
    protected HodFieldsRequest createRequest() {
        return new HodFieldsRequest.Builder().setDatabases(Collections.singleton(ResourceIdentifier.WIKI_ENG)).build();
    }
}
