/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointApiClientTest;
import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class ControlPointConfigValidatorTest {
    @Mock private ConfigService<IdolFindConfig> configService;
    @Mock private HttpClient httpClient;
    private ControlPointConfigValidator validator;

    @Before
    public void setUp() throws IOException {
        Mockito.when(configService.getConfig()).thenReturn(IdolFindConfig.builder()
            .controlPoint(new ControlPointConfig(true, ControlPointServerConfigTest.validConfig))
            .build());
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildLoginResponse())
            .thenReturn(ControlPointApiClientTest.buildResponse(200, "OK", "[]"));
        validator = new ControlPointConfigValidator(configService, httpClient);
    }

    @Test
    public void testValidate_valid() {
        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertTrue("should report valid", res.isValid());
    }

    @Test
    public void testValidate_invalidCredentials() throws IOException {
        final String resJson = "{" +
            "\"error\": \"Invalid Grant\"," +
            "\"error_description\":\"Wrong credentials\"}";
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildResponse(401, "Unauthorized", resJson));

        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "INVALID_CREDENTIALS", res.getData());
    }

    @Test
    public void testValidate_apiError() throws IOException {
        final String resJson = "{" +
            "\"error\": \"Unknown\"," +
            "\"error_description\":\"Bad things\"}";
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildResponse(400, "Bad Request", resJson));

        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "UNKNOWN_ERROR", res.getData());
    }

    @Test
    public void testValidate_connectionError() throws IOException {
        Mockito.when(httpClient.execute(Mockito.any())).thenThrow(new IOException());

        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "CONNECTION_ERROR", res.getData());
    }

}
