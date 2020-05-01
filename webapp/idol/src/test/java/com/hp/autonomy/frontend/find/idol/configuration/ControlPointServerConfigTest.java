/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointServerDetails;
import org.junit.Assert;
import org.junit.Test;

public class ControlPointServerConfigTest {
    static final ControlPointServerConfig validConfig = ControlPointServerConfig.builder()
        .protocol("HTTP").host("example.com").port(123).basePath("base/path")
        .credentials(new CredentialsConfig("user", "pass"))
        .build();

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        validConfig.basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_missingProtocol() throws ConfigException {
        validConfig.toBuilder().protocol(null).build().basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_unknownProtocol() throws ConfigException {
        validConfig.toBuilder().protocol("FTP").build().basicValidate("cp");
    }

    @Test
    public void testBasicValidate_lowercaseProtocol() throws ConfigException {
        validConfig.toBuilder().protocol("https").build().basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_missingHost() throws ConfigException {
        validConfig.toBuilder().host(null).build().basicValidate("cp");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_missingPort() throws ConfigException {
        validConfig.toBuilder().port(null).build().basicValidate("cp");
    }

    @Test
    public void testBasicValidate_missingCredentials() throws ConfigException {
        validConfig.toBuilder().credentials(null).build().basicValidate("cp");
    }

    @Test
    public void testBasicValidate_missingbasePath() throws ConfigException {
        validConfig.toBuilder().basePath(null).build().basicValidate("cp");
    }

    @Test
    public void testToServerDetails() throws ConfigException {
        final ControlPointServerDetails serverDetails = validConfig.toServerDetails();
        Assert.assertEquals("should set lowercase protocol", "http", serverDetails.getProtocol());
        Assert.assertEquals("should set host", "example.com", serverDetails.getHost());
        Assert.assertEquals("should set port", 123, serverDetails.getPort());
        Assert.assertEquals("should set username", "user", serverDetails.getUsername());
        Assert.assertEquals("should set password", "pass", serverDetails.getPassword());
        Assert.assertEquals("should set base path", "base/path", serverDetails.getBasePath());
    }

    @Test
    public void testToServerDetails_missingBasePath() throws ConfigException {
        final ControlPointServerDetails serverDetails =
            validConfig.toBuilder().basePath(null).build().toServerDetails();
        // leaving it at the default value
        Assert.assertEquals("should not set base path", "/WebApi/api", serverDetails.getBasePath());
    }

}
