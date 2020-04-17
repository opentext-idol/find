/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointServerDetails;
import org.junit.Assert;
import org.junit.Test;

public class ControlPointConfigTest {

    @Test
    public void testBasicValidate_disabled() throws ConfigException {
        new ControlPointConfig(false, null).basicValidate("cp");
    }

    @Test
    public void testBasicValidate_enabled() throws ConfigException {
        new ControlPointConfig(true, ControlPointServerConfigTest.validConfig).basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_missingServer() throws ConfigException {
        new ControlPointConfig(true, null).basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_invalidServer() throws ConfigException {
        final ControlPointServerConfig invalidServer =
            ControlPointServerConfigTest.validConfig.toBuilder().protocol(null).build();
        new ControlPointConfig(true, invalidServer).basicValidate("cp");
    }

}
