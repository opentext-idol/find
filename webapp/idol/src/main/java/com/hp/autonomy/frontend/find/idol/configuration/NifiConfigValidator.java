/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.frontend.find.idol.nifi.NifiService;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator for NiFi configuration.
 */
@Component
@Slf4j
public class NifiConfigValidator implements Validator<NifiConfig> {
    private final ProcessorFactory processorFactory;
    private final AciService aciService;

    @Autowired
    public NifiConfigValidator(
        final ProcessorFactory processorFactory,
        final AciService aciService
    ) {
        this.processorFactory = processorFactory;
        this.aciService = aciService;
    }

    @Override
    public ValidationResult<String> validate(final NifiConfig newConfig) {
        // want to be able to validate config details even if disabled
        final NifiConfig config = newConfig.toBuilder().enabled(true).build();
        final NifiService nifiService = new NifiService(processorFactory, aciService, config);

        try {
            nifiService.checkStatus();
        } catch (final RuntimeException e) {
            log.error("Error accessing NiFi", e);
            return new ValidationResult<>(false, "CONNECTION_ERROR");
        }

        return new ValidationResult<>(true);
    }

    @Override
    public Class<NifiConfig> getSupportedClass() {
        return NifiConfig.class;
    }

}
