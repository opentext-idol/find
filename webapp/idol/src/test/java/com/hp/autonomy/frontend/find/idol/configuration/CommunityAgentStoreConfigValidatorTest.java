/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommunityAgentStoreConfigValidatorTest {
    @Mock private AciService aciService;
    @Mock private ProcessorFactory processorFactory;
    @Mock private ServerConfig serverConfig;
    private CommunityAgentStoreConfigValidator validator;

    @Before
    public void setUp() {
        validator = new CommunityAgentStoreConfigValidator(aciService, processorFactory);
    }

    @Test
    public void testValidate_valid() throws ConfigException {
        Mockito.when(serverConfig.validate(aciService, null, processorFactory))
            .thenReturn(new ValidationResult<>(true));
        final CommunityAgentStoreConfig config =
            CommunityAgentStoreConfig.builder().server(serverConfig).build();
        final ValidationResult<?> result = validator.validate(config);
        Assert.assertTrue(result.isValid());
    }

    @Test
    public void testValidate_invalid() throws ConfigException {
        Mockito.when(serverConfig.validate(aciService, null, processorFactory))
            .thenReturn(new ValidationResult<>(false));
        final CommunityAgentStoreConfig config =
            CommunityAgentStoreConfig.builder().server(serverConfig).build();
        final ValidationResult<?> result = validator.validate(config);
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidate_noServerConfig() throws ConfigException {
        final CommunityAgentStoreConfig config = CommunityAgentStoreConfig.builder().build();
        final ValidationResult<?> result = validator.validate(config);
        Assert.assertTrue(result.isValid());
    }

}
