/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = TrendingConfiguration.TrendingConfigurationBuilder.class)
public class TrendingConfiguration extends SimpleComponent<TrendingConfiguration> {
    private final FieldPath dateField;
    private final Integer numberOfBuckets;
    private final Integer numberOfValues;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if(dateField == null || dateField.getNormalisedPath().isEmpty()) {
            throw new ConfigException(configSection, "dateField must be provided");
        }

        if(numberOfBuckets <= 0) {
            throw new ConfigException(configSection, "numberOfBuckets must be provided and greater than 0");
        }

        if(numberOfValues <= 0) {
            throw new ConfigException(configSection, "numberOfValues must be provided and greater than 0");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class TrendingConfigurationBuilder {}
}
