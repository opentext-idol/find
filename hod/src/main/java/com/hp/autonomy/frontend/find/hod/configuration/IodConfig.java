/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.Resources;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
@JsonDeserialize(builder = IodConfig.Builder.class)
public class IodConfig implements ConfigurationComponent {
    private final ApiKey apiKey;
    private final List<ResourceIdentifier> activeIndexes;
    private final Boolean publicIndexesEnabled;

    private IodConfig(final Builder builder) {
        this.apiKey = builder.apiKey;
        this.activeIndexes = builder.activeIndexes;
        this.publicIndexesEnabled = builder.publicIndexesEnabled;
    }

    public List<ResourceIdentifier> getActiveIndexes() {
        return activeIndexes == null ? Collections.<ResourceIdentifier>emptyList() : new ArrayList<>(activeIndexes);
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public IodConfig merge(final IodConfig iod) {
        if (iod != null) {
            return new Builder()
                    .setApiKey(apiKey == null ? iod.apiKey : apiKey)
                    .setActiveIndexes(activeIndexes == null ? iod.activeIndexes : activeIndexes)
                    .setPublicIndexesEnabled(publicIndexesEnabled == null ? iod.publicIndexesEnabled : publicIndexesEnabled)
                    .build();
        } else {
            return this;
        }
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ApiKey apiKey;
        private List<ResourceIdentifier> activeIndexes;
        private Boolean publicIndexesEnabled;

        public IodConfig build() {
            return new IodConfig(this);
        }
    }
}
