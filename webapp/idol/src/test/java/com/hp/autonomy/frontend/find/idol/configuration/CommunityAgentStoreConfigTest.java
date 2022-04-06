/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import org.junit.Test;

public class CommunityAgentStoreConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder().build();
        config.basicValidate("agentstore");
    }

    @Test
    public void testBasicValidate_disabled() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder()
            .enabled(false)
            .server(null)
            .build();
        config.basicValidate("agentstore");
    }

    @Test
    public void testBasicValidate_enabled() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder()
            .enabled(true)
            .server(ServerConfig.builder().host("localhost").port(123).build())
            .build();
        config.basicValidate("agentstore");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_serverMissing() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder()
            .enabled(true)
            .server(null)
            .build();
        config.basicValidate("agentstore");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_serverInvalid() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder()
            .enabled(true)
            .server(ServerConfig.builder().build())
            .build();
        config.basicValidate("agentstore");
    }

}
