/*
 * Copyright 2020 Open Text.
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
