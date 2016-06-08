/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.google.common.collect.ImmutableList;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.mockito.Matchers;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class HodFieldsControllerTest extends AbstractFieldsControllerTest<HodFieldsRequest, HodErrorException> {
    @Override
    protected FieldsController<HodFieldsRequest, HodErrorException> constructController() {
        return new HodFieldsController(service);
    }

    @Override
    protected HodFieldsRequest createRequest() {
        return new HodFieldsRequest.Builder().setDatabases(Collections.singleton(ResourceIdentifier.WIKI_ENG)).build();
    }

    @Override
    public void getParametricDateFields() throws HodErrorException {
        // TODO: remove onceFIND-180 is complete
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(new TagName("DateField"), new TagName("ParametricDateField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<HodFieldsRequest>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);
        final List<TagName> fields = controller.getParametricDateFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new TagName("ParametricDateField"))));
    }
}
