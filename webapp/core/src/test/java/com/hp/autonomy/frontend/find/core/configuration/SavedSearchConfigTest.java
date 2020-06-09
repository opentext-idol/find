/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
