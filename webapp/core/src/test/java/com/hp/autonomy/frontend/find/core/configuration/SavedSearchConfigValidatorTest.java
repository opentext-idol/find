/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SavedSearchConfigValidatorTest {
    private SavedSearchConfigValidator savedSearchConfigValidator;

    @Before
    public void setUp() {
        savedSearchConfigValidator = new SavedSearchConfigValidator();
    }

    @Test
    public void basicValidate() {
        assertTrue(savedSearchConfigValidator.validate(new SavedSearchConfig.Builder()
                .setPollForUpdates(true)
                .setPollingInterval(5)
                .build()
        ).isValid());
    }

    @Test
    public void basicValidateWhenDisabled() throws ConfigException {
        assertTrue(savedSearchConfigValidator.validate(new SavedSearchConfig.Builder()
                .setPollForUpdates(false)
                .setPollingInterval(-1)
                .build()
        ).isValid());
    }

    @Test
    public void basicValidateWhenInvalid() throws ConfigException {
        assertFalse(savedSearchConfigValidator.validate(new SavedSearchConfig.Builder()
                .setPollForUpdates(true)
                .setPollingInterval(-1)
                .build()
        ).isValid());
    }

    @Test
    public void getSupportedClass() {
        assertEquals(SavedSearchConfig.class, savedSearchConfigValidator.getSupportedClass());
    }
}
