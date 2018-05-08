/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Collection;

@SuppressWarnings("DefaultAnnotationParam")
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = HodConfig.HodConfigBuilder.class)
public class HodConfig extends SimpleComponent<HodConfig> implements OptionalConfigurationComponent<HodConfig> {
    private final ApiKey apiKey;
    @Singular
    private final Collection<ResourceName> activeIndexes;
    private final Boolean publicIndexesEnabled;
    private final URL ssoPageGetUrl;
    private final URL ssoPagePostUrl;
    private final URL endpointUrl;

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
        return true;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (apiKey == null || StringUtils.isEmpty(apiKey.getApiKey())) {
            throw new ConfigException(section, "Application API key must be provided");
        }

        if (publicIndexesEnabled == null) {
            throw new ConfigException(section, "The publicIndexesEnabled option must be specified");
        }

        if (ssoPageGetUrl == null || ssoPagePostUrl == null) {
            throw new ConfigException(section, "Both URLs for the SSO page must be provided");
        }

        if (endpointUrl == null) {
            throw new ConfigException(section, "The endpoint URL must be provided");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class HodConfigBuilder {
    }
}
