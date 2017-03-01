/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.URL;

@Getter
@Builder
@SuppressWarnings("DefaultAnnotationParam")
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = HsodConfig.HsodConfigBuilder.class)
public class HsodConfig extends SimpleComponent<HsodConfig> implements ConfigurationComponent<HsodConfig> {
    private final URL landingPageUrl;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (landingPageUrl == null) {
            throw new ConfigException(section, "Landing page URL must be provided");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class HsodConfigBuilder {
    }
}
