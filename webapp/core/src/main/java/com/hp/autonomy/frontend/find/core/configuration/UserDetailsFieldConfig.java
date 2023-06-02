/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
