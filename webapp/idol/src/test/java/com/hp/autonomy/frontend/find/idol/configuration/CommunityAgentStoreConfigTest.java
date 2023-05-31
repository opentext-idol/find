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
