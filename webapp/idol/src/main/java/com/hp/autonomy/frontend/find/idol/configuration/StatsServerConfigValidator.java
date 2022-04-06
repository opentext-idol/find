/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
