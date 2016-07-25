/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Controller
@Slf4j
class HodFieldsController extends FieldsController<HodFieldsRequest, HodErrorException, ResourceIdentifier, HodQueryRestrictions, HodParametricRequest> {
    @Autowired
    HodFieldsController(
            final FieldsService<HodFieldsRequest, HodErrorException> fieldsService,
            final ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> parametricValuesService,
            final ObjectFactory<ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier>> parametricRequestBuilderFactory
    ) {
        super(fieldsService, parametricValuesService, parametricRequestBuilderFactory);
    }

    @Override
    protected HodQueryRestrictions createValueDetailsQueryRestrictions(final HodFieldsRequest request) {
        return new HodQueryRestrictions.Builder()
                .setQueryText("*")
                .setDatabases(new LinkedList<>(request.getDatabases()))
                .build();
    }

    @Override
    public List<FieldAndValueDetails> getParametricDateFields(final HodFieldsRequest request) throws HodErrorException {
        // TODO: Remove this override once FIND-180 is complete; we are just preventing AUTN_DATE from showing up in HoD as it will cause performance problems
        return fetchParametricFieldAndValueDetails(request, FieldTypeParam.NumericDate, Collections.<String>emptyList());
    }
}
