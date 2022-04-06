/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import org.junit.Test;

public class NifiConfigTest {
    private static final ServerConfig validServer =
        ServerConfig.builder().host("host").port(123).build();

    @Test
    public void testBasicValidate_disabled() throws ConfigException {
        new NifiConfig(false, null, "listactions").basicValidate("nifi");
    }

    @Test
    public void testBasicValidate_enabled() throws ConfigException {
        new NifiConfig(true, validServer, "listactions").basicValidate("nifi");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_missingServer() throws ConfigException {
        new NifiConfig(true, null, "listactions").basicValidate("nifi");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_invalidServer() throws ConfigException {
        final ServerConfig invalidServer =
            ServerConfig.builder().host("host").port(-5).build();
        new NifiConfig(true, invalidServer, "listactions").basicValidate("nifi");
    }

}
