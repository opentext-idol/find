/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SavedSearchConfigTest {
    private SavedSearchConfig savedSearchConfig;

    @Before
    public void setUp() {
        savedSearchConfig = new SavedSearchConfig.Builder()
            .setPollForUpdates(true)
            .setPollingInterval(5)
            .build();
    }

    @Test
    public void merge() {
        assertEquals(savedSearchConfig, new SavedSearchConfig.Builder().build().merge(savedSearchConfig));
    }

    @Test
    public void mergeNoDefaults() {
        assertEquals(savedSearchConfig, savedSearchConfig.merge(null));
    }

    @Test
    public void basicValidate() throws ConfigException {
        savedSearchConfig.basicValidate(null);
    }

    @Test
    public void basicValidateWhenDisabled() throws ConfigException {
        new SavedSearchConfig.Builder()
            .setPollForUpdates(false)
            .setPollingInterval(-1)
            .build()
            .basicValidate(null);
    }

    @Test
    public void basicValidateWhenInvalid() throws ConfigException {
        try {
            new SavedSearchConfig.Builder()
                .setPollForUpdates(true)
                .setPollingInterval(-1)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Polling interval must be positive"));
        }
    }
}
