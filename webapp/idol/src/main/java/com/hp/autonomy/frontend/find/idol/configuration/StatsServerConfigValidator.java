/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.searchcomponents.idol.statsserver.Statistic;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class StatsServerConfigValidator implements Validator<StatsServerConfig> {

    private final AciService aciService;
    private final ProcessorFactory processorFactory;
    private final IdolAnnotationsProcessorFactory annotationsProcessorFactory;

    @Resource(name = "requiredStatistics")
    private Set<Statistic> requiredStatistics;

    @Autowired
    public StatsServerConfigValidator(final AciService aciService, final ProcessorFactory processorFactory, final IdolAnnotationsProcessorFactory annotationsProcessorFactory) {
        this.aciService = aciService;
        this.processorFactory = processorFactory;
        this.annotationsProcessorFactory = annotationsProcessorFactory;
    }

    @Override
    public ValidationResult<?> validate(final StatsServerConfig config) {
        return config.validate(aciService, requiredStatistics, processorFactory, annotationsProcessorFactory);
    }

    @Override
    public Class<StatsServerConfig> getSupportedClass() {
        return StatsServerConfig.class;
    }

}
