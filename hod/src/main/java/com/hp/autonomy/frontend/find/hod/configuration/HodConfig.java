/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
@JsonDeserialize(builder = HodConfig.Builder.class)
public class HodConfig implements ConfigurationComponent, com.hp.autonomy.frontend.find.core.configuration.ConfigurationComponent<HodConfig> {
    private final ApiKey apiKey;
    private final List<ResourceIdentifier> activeIndexes;
    private final Boolean publicIndexesEnabled;

    private HodConfig(final Builder builder) {
        apiKey = builder.apiKey;
        activeIndexes = builder.activeIndexes;
        publicIndexesEnabled = builder.publicIndexesEnabled;
    }

    public List<ResourceIdentifier> getActiveIndexes() {
        return activeIndexes == null ? Collections.emptyList() : new ArrayList<>(activeIndexes);
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public HodConfig merge(final HodConfig other) {
        if (other == null) {
            return this;
        } else {
            return new Builder()
                    .setApiKey(apiKey == null ? other.apiKey : apiKey)
                    .setActiveIndexes(activeIndexes == null ? other.activeIndexes : activeIndexes)
                    .setPublicIndexesEnabled(publicIndexesEnabled == null ? other.publicIndexesEnabled : publicIndexesEnabled)
                    .build();
        }
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (apiKey == null || StringUtils.isEmpty(apiKey.getApiKey())) {
            throw new ConfigException(section, "Application API key must be provided");
        }

        if (publicIndexesEnabled == null) {
            throw new ConfigException(section, "The publicIndexesEnabled option must be specified");
        }
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ApiKey apiKey;
        private List<ResourceIdentifier> activeIndexes;
        private Boolean publicIndexesEnabled;

        public HodConfig build() {
            return new HodConfig(this);
        }
    }
}
