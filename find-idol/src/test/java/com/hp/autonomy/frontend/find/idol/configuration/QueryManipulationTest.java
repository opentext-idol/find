/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryManipulationTest {
    private QueryManipulation queryManipulation;

    @Before
    public void setUp() {
        final ServerConfig serverConfig = new ServerConfig.Builder().setHost("find-idol").setPort(16000).build();
        queryManipulation = new QueryManipulation.Builder()
                .setServer(serverConfig)
                .setExpandQuery(true)
                .setBlacklist("ISO_Blacklist")
                .setEnabled(true)
                .build();
    }

    @Test
    public void validateGoodConfig() throws ConfigException {
        queryManipulation.basicValidate();
    }

    @Test(expected = ConfigException.class)
    public void validateBadConfig() throws ConfigException {
        queryManipulation = new QueryManipulation.Builder()
                .setEnabled(true)
                .build();
        queryManipulation.basicValidate();
    }

    @Test
    public void disabled() throws ConfigException {
        queryManipulation = new QueryManipulation.Builder()
                .build();
        queryManipulation.basicValidate();
    }

    @Test
    public void merge() {
        final String blacklist = "OSI_Blacklist";
        final QueryManipulation defaults = new QueryManipulation.Builder().setBlacklist(blacklist).build();
        final QueryManipulation mergedConfig = queryManipulation.merge(defaults);
        assertNotNull(mergedConfig.getServer());
        assertTrue(mergedConfig.getExpandQuery());
        assertEquals("ISO_Blacklist", mergedConfig.getBlacklist());
        assertTrue(mergedConfig.getEnabled());
        assertTrue(mergedConfig.isEnabled());
    }

    @Test
    public void mergeWithNoDefaults() {
        assertEquals(queryManipulation, queryManipulation.merge(null));
    }
}
