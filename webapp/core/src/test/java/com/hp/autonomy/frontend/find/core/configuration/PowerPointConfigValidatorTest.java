/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PowerPointConfigValidatorTest {
    private PowerPointConfigValidator powerPointConfigValidator;

    @Before
    public void setUp() {
        powerPointConfigValidator = new PowerPointConfigValidator();
    }

    @Test
    public void basicValidate() {
        assertTrue(powerPointConfigValidator.validate(new PowerPointConfig.Builder().build()).isValid());
    }

    @Test
    public void basicValidateWhenInvalid() throws ConfigException {
        assertFalse(powerPointConfigValidator.validate(new PowerPointConfig.Builder()
                .setMarginBottom(10.0)
                .build()
        ).isValid());
    }

    @Test
    public void getSupportedClass() {
        assertEquals(PowerPointConfig.class, powerPointConfigValidator.getSupportedClass());
    }
}
