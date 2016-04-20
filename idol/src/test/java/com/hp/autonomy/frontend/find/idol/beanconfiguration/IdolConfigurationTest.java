/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.ConfigFileConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryConfiguration;
import com.hp.autonomy.frontend.find.core.test.TestConfiguration;
import com.hp.autonomy.frontend.find.idol.search.IdolQueryRestrictionsDeserializer;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigFileConfiguration.class, InMemoryConfiguration.class, IdolConfiguration.class, TestConfiguration.class, HavenSearchIdolConfiguration.class, IdolQueryRestrictionsDeserializer.class})
@TestPropertySource(properties = "hp.find.persistentState = INMEMORY")
public class IdolConfigurationTest {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("hp.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
    }

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private IdolConfiguration idolConfiguration;

    @Test
    public void wiring() {
        assertNotNull(idolConfiguration);
    }
}
