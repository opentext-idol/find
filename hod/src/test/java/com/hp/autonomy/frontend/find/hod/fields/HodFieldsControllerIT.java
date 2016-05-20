/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.fields.FieldsControllerIT;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringApplicationConfiguration(classes = HodFindApplication.class)
public class HodFieldsControllerIT extends FieldsControllerIT {
    @Override
    protected void addParams(final MockHttpServletRequestBuilder requestBuilder) {
        requestBuilder.param("databases", mvcIntegrationTestUtils.getDatabases());
    }
}
