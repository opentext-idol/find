/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsonDeserialize(builder = SavedSearchConfig.Builder.class)
@Data
public class SavedSearchConfig implements ConfigurationComponent {
    private final Boolean pollForUpdates;
    private final Integer pollingInterval;

    private SavedSearchConfig(final Builder builder) {
        pollForUpdates = builder.pollForUpdates;
        pollingInterval = builder.pollingInterval;
    }

    public SavedSearchConfig merge(final SavedSearchConfig savedSearchConfig) {
        return savedSearchConfig != null ?
                new SavedSearchConfig.Builder()
                        .setPollForUpdates(pollForUpdates == null ? savedSearchConfig.pollForUpdates : pollForUpdates)
                        .setPollingInterval(pollingInterval == null ? savedSearchConfig.pollingInterval : pollingInterval)
                        .build()
                : this;
    }

    public void basicValidate() throws ConfigException {
        if (pollForUpdates != null && pollForUpdates && (pollingInterval == null || pollingInterval <= 0)) {
            throw new ConfigException("Saved Searches", "Polling interval must be positive");
        }
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private Boolean pollForUpdates;
        private Integer pollingInterval;

        public SavedSearchConfig build() {
            return new SavedSearchConfig(this);
        }
    }
}
