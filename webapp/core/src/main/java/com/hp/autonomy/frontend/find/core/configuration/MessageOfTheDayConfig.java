/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;

@JsonDeserialize(builder = MessageOfTheDayConfig.MessageOfTheDayConfigBuilder.class)
@Builder(toBuilder = true)
@Data
public class MessageOfTheDayConfig implements OptionalConfigurationComponent<MessageOfTheDayConfig> {
    private final String message;
    private final String cssClass;

    @Override
    public MessageOfTheDayConfig merge(final MessageOfTheDayConfig other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {}

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class MessageOfTheDayConfigBuilder {}
}
