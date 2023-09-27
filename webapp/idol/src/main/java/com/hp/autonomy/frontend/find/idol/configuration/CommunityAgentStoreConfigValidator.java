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

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.opentext.idol.types.marshalling.ProcessorFactory;
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
