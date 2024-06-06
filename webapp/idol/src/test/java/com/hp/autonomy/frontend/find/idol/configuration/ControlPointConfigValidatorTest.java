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

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointApiClientTest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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
        Mockito.doAnswer(ControlPointApiClientTest.buildLoginAnswer())
            .doAnswer(ControlPointApiClientTest.buildAnswer(200, "[]"))
            .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
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
        Mockito.doAnswer(ControlPointApiClientTest.buildAnswer(401, resJson))
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

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
        Mockito.doAnswer(ControlPointApiClientTest.buildAnswer(400, resJson))
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "UNKNOWN_ERROR", res.getData());
    }

    @Test
    public void testValidate_connectionError() throws IOException {
        Mockito.doThrow(new IOException())
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final ValidationResult<String> res = validator.validate(
            new ControlPointConfig(true, ControlPointServerConfigTest.validConfig));
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "CONNECTION_ERROR", res.getData());
    }

}
