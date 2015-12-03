/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web;

import com.hp.autonomy.frontend.find.core.beanconfiguration.ConfigFileConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryConfiguration;
import com.hp.autonomy.frontend.find.core.search.DocumentsService;
import com.hp.autonomy.frontend.find.hod.search.HodFindDocument;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigFileConfiguration.class, InMemoryConfiguration.class, HodConfigurationLoader.class})
@TestPropertySource(properties = "hp.find.persistentState = INMEMORY")
public class HodConfigurationLoaderTest {
    @BeforeClass
    public static void init() {
        System.setProperty("hp.find.home", "./src/test/resources");
    }

    @Autowired
    private DocumentsService<ResourceIdentifier, HodFindDocument, HodErrorException> documentsService;

    @Test
    public void hodModuleStartup() {
        assertNotNull(documentsService);
    }
}
