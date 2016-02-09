/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.test;

import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.springframework.stereotype.Component;

@Component
public class IdolMvcIntegrationTestUtils extends MvcIntegrationTestUtils {
    @Override
    public String[] getDatabases() {
        return new String[]{"Wookiepedia"};
    }

    @Override
    public String[] getParametricFields() {
        return new String[]{"CATEGORY", "AUTHOR"};
    }
}
