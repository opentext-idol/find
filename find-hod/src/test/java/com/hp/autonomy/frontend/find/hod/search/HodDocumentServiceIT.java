/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentServiceIT;
import com.hp.autonomy.frontend.find.web.test.HodTestConfiguration;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.junit.BeforeClass;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.io.IOException;

@SpringApplicationConfiguration(classes = HodFindApplication.class)
public class HodDocumentServiceIT extends AbstractDocumentServiceIT {
    @BeforeClass
    public static void startup() throws IOException {
        HodTestConfiguration.writeConfigFile(TEST_DIR);
    }

    public HodDocumentServiceIT() {
        super(new String[]{ResourceIdentifier.NEWS_ENG.toString()});
    }
}
