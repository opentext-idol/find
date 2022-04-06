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
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.util.AciParameters;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.searchcomponents.idol.statsserver.Statistic;
import com.hp.autonomy.searchcomponents.idol.statsserver.StatisticProcessor;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;

@Data
@JsonDeserialize(builder = StatsServerConfig.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class StatsServerConfig implements OptionalConfigurationComponent<StatsServerConfig> {

    private final ServerConfig server;
    private final Boolean enabled;

    @Override
    public StatsServerConfig merge(final StatsServerConfig other) {
        if (other == null) {
            return this;
        }

        return new Builder()
                .setEnabled(enabled == null ? other.enabled : enabled)
                .setServer(server == null ? other.server : server.merge(other.server))
                .build();
    }

    @Override
    public void basicValidate(final String s) throws ConfigException {
    }

    ValidationResult<?> validate(final AciService aciService, final Collection<Statistic> requiredStatistics, final ProcessorFactory processorFactory, final IdolAnnotationsProcessorFactory annotationsProcessorFactory) {
        final ValidationResult<?> serverResult =  server.validate(aciService, null, processorFactory);

        if (!serverResult.isValid()) {
            return serverResult;
        }

        final Set<Statistic> statistics;

        try {
            statistics = aciService.executeAction(server.toAciServerDetails(), new AciParameters("GetStatus"), new StatisticProcessor(annotationsProcessorFactory));
        } catch (final ProcessorException | AciServiceException ignored) {
            return new ValidationResult<>(false, ValidationKey.CONNECTION_ERROR);
        }

        if (!statistics.containsAll(requiredStatistics)) {
            statistics.stream().filter(statistic -> !requiredStatistics.contains(statistic)).forEach(statistic -> log.debug("Additional statistic present in StatsServer: {}", statistic));

            requiredStatistics.stream().filter(requiredStatistic -> !statistics.contains(requiredStatistic)).forEach(requiredStatistic -> log.debug("Required statistic missing from StatsServer: {}", requiredStatistic));

            return new ValidationResult<>(false, ValidationKey.INVALID_CONFIGURATION);
        }

        return new ValidationResult<>(true);
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ServerConfig server;
        private Boolean enabled;

        public StatsServerConfig build() {
            return new StatsServerConfig(server, enabled);
        }
    }

    private enum ValidationKey {
        CONNECTION_ERROR, INVALID_CONFIGURATION
    }

}
