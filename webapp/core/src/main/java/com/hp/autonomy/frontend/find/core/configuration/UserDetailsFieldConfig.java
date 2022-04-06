/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = UserDetailsFieldConfig.UserDetailsFieldConfigBuilder.class)
public class UserDetailsFieldConfig extends SimpleComponent<UserDetailsFieldConfig> {
    private final String name;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (name == null) {
            throw new ConfigException(configSection, "name must be provided");
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDetailsFieldConfigBuilder {
    }

}
