/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsonDeserialize(builder = SavedSearchConfig.Builder.class)
@Data
public class SavedSearchConfig implements OptionalConfigurationComponent<SavedSearchConfig> {
    private final Boolean pollForUpdates;
    private final Integer pollingInterval;

    private SavedSearchConfig(final Builder builder) {
        pollForUpdates = builder.pollForUpdates;
        pollingInterval = builder.pollingInterval;
    }

    @Override
    public SavedSearchConfig merge(final SavedSearchConfig savedSearchConfig) {
        return savedSearchConfig != null ?
                new SavedSearchConfig.Builder()
                        .setPollForUpdates(pollForUpdates == null ? savedSearchConfig.pollForUpdates : pollForUpdates)
                        .setPollingInterval(pollingInterval == null ? savedSearchConfig.pollingInterval : pollingInterval)
                        .build()
                : this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if(pollForUpdates != null && pollForUpdates && (pollingInterval == null || pollingInterval <= 0)) {
            throw new ConfigException("Saved Searches", "Polling interval must be positive");
        }
    }

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
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
