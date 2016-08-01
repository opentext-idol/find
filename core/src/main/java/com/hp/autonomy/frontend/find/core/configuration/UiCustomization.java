/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Getter;
import lombok.experimental.Builder;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Builder(fluent = false)
@Getter
@JsonDeserialize(builder = UiCustomization.UiCustomizationBuilder.class)
public class UiCustomization implements ConfigurationComponent<UiCustomization> {
    private final UiCustomizationOptions options;

    @Override
    public UiCustomization merge(final UiCustomization uiCustomization) {
        return uiCustomization == null ? this : builder()
                .setOptions(options.merge(uiCustomization.options))
                .build();
    }

    @Override
    public void basicValidate(final String... options) throws ConfigException {
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "set")
    public static class UiCustomizationBuilder {}
}
