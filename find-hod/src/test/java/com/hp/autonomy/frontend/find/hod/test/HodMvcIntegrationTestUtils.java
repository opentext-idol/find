/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.test;

import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.springframework.stereotype.Component;

@Component
public class HodMvcIntegrationTestUtils extends MvcIntegrationTestUtils {
    @Override
    public String[] getDatabases() {
        return new String[]{ResourceIdentifier.WIKI_ENG.toString()};
    }

    @Override
    public String[] getParametricFields() {
        return new String[]{"WIKIPEDIA_CATEGORY"};
    }
}
