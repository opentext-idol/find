/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = ThemeTrackerConfiguration.ThemeTrackerConfigurationBuilder.class)
public class ThemeTrackerConfiguration extends SimpleComponent<ThemeTrackerConfiguration> {

    private final List<String> databaseNames;
    private final Double minScore;
    private final String host;
    private final int port;
    private final String jobName;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class ThemeTrackerConfigurationBuilder {
    }
}
