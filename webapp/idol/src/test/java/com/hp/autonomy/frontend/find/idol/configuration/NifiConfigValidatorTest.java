/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.idol.controlpoint.NifiServiceTest;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class NifiConfigValidatorTest {
    @Mock private ProcessorFactory processorFactory;
    @Mock private AciService aciService;
    private NifiConfigValidator validator;

    @Before
    public void setUp() throws IOException {
        Mockito.doAnswer(inv -> {
            return inv.getArgumentAt(2, Processor.class)
                .process(NifiServiceTest.buildResponse(NifiServiceTest.listActionsResponse));
        }).when(aciService).executeAction(any(), any(), any());
        validator = new NifiConfigValidator(processorFactory, aciService);
    }

    @Test
    public void testValidate_valid() {
        final ValidationResult<String> res = validator.validate(NifiServiceTest.validConfig);
        Assert.assertTrue("should report valid", res.isValid());
    }

    @Test
    public void testValidate_error() throws IOException {
        Mockito.doThrow(new AciServiceException("connection error"))
            .when(aciService).executeAction(any(), any(), any());
        final ValidationResult<String> res = validator.validate(NifiServiceTest.validConfig);
        Assert.assertFalse("should report invalid", res.isValid());
        Assert.assertEquals("should have correct error ID", "CONNECTION_ERROR", res.getData());
    }

}
