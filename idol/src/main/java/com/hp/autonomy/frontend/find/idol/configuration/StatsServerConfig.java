/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.util.AciParameters;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.searchcomponents.idol.statsserver.Statistic;
import com.hp.autonomy.searchcomponents.idol.statsserver.StatisticProcessor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;

@Data
@JsonDeserialize(builder = StatsServerConfig.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class StatsServerConfig implements ConfigurationComponent {

    private final ServerConfig server;

    @Getter(AccessLevel.NONE)
    private final Boolean enabled;

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
    public boolean isEnabled() {
        return enabled;
    }

    public ValidationResult<?> validate(final AciService aciService, final Collection<Statistic> requiredStatistics, final IdolAnnotationsProcessorFactory processorFactory) {
        final ValidationResult<?> serverResult =  server.validate(aciService, null, processorFactory);

        if (!serverResult.isValid()) {
            return serverResult;
        }

        final Set<Statistic> statistics;

        try {
            statistics = aciService.executeAction(server.toAciServerDetails(), new AciParameters("GetStatus"), new StatisticProcessor(processorFactory));
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

    private enum ValidationKey  {
        CONNECTION_ERROR, INVALID_CONFIGURATION
    }

}
