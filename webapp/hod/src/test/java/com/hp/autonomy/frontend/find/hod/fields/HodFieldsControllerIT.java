/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.FieldsControllerIT;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class HodFieldsControllerIT extends FieldsControllerIT {
    @Override
    protected void addParams(final MockHttpServletRequestBuilder requestBuilder) {
        requestBuilder.param(HodFieldsController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases());
    }
}
