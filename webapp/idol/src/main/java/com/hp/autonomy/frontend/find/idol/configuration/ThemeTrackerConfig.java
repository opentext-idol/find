/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@JsonDeserialize(builder = ThemeTrackerConfig.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ThemeTrackerConfig implements OptionalConfigurationComponent<ThemeTrackerConfig> {

    private final ServerConfig category;
    private final Boolean enabled;
    private final List<String> databaseNames;
    private final Double minScore;
    private final String jobName;

    @Override
    public ThemeTrackerConfig merge(final ThemeTrackerConfig other) {
        if (other == null) {
            return this;
        }

        return new Builder()
                .setEnabled(enabled == null ? other.enabled : enabled)
                .setCategory(category == null ? other.category : category.merge(other.category))
                .setDatabaseNames(databaseNames == null ? other.databaseNames : databaseNames)
                .setMinScore(minScore == null ? other.minScore : minScore)
                .setJobName(jobName == null ? other.jobName : jobName)
                .build();
    }

    @Override
    public void basicValidate(final String s) throws ConfigException {
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ServerConfig category;
        private Boolean enabled;
        private List<String> databaseNames;
        private Double minScore;
        private String jobName;

        public ThemeTrackerConfig build() {
            return new ThemeTrackerConfig(category, enabled, databaseNames, minScore, jobName);
        }
    }
}
