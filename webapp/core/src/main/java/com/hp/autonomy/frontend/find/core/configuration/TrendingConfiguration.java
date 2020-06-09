/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    private static final String SECTION = "Trending";

    private final FieldPath dateField;
    private final Integer defaultNumberOfBuckets;
    private final Integer minNumberOfBuckets;
    private final Integer maxNumberOfBuckets;
    private final Integer numberOfValues;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (dateField == null || dateField.getNormalisedPath().isEmpty()) {
            throw new ConfigException(configSection, "dateField must be provided");
        }

        validateInteger(defaultNumberOfBuckets, "Default number of buckets");
        validateInteger(minNumberOfBuckets, "Minimum number of buckets");
        validateInteger(maxNumberOfBuckets, "Maximum number of buckets");
        validateInteger(numberOfValues, "Number of values");

        if (maxNumberOfBuckets < defaultNumberOfBuckets || minNumberOfBuckets > defaultNumberOfBuckets) {
            throw new ConfigException(configSection, "Default number of buckets must lie between max and min");
        }
    }

    private void validateInteger(final Integer integer, final String description) throws ConfigException {
        if (integer == null || integer <= 0) {
            throw new ConfigException(SECTION, description + " must be provided and must be greater than 0");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class TrendingConfigurationBuilder {
    }
}
