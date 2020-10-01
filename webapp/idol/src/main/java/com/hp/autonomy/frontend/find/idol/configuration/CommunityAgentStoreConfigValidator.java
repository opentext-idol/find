/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.nonaci.indexing.IndexingService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommunityAgentStoreConfigValidator implements Validator<CommunityAgentStoreConfig> {
    private final AciService aciService;
    private final ProcessorFactory processorFactory;

    @Autowired
    public CommunityAgentStoreConfigValidator(
        final AciService aciService,
        final ProcessorFactory processorFactory
    ) {
        this.aciService = aciService;
        this.processorFactory = processorFactory;
    }

    @Override
    public ValidationResult<?> validate(final CommunityAgentStoreConfig config) {
        if (config.getServer() != null) {
            return config.getServer().validate(aciService, null, processorFactory);
        } else {
            return new ValidationResult<>(true);
        }
    }

    @Override
    public Class<CommunityAgentStoreConfig> getSupportedClass() {
        return CommunityAgentStoreConfig.class;
    }

}
