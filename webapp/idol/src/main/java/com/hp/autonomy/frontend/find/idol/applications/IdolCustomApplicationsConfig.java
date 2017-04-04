/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.applications;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = IdolCustomApplicationsConfig.IdolCustomApplicationsConfigBuilder.class)
public class IdolCustomApplicationsConfig extends AbstractConfig<IdolCustomApplicationsConfig> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Singular
    private final Collection<IdolCustomApplication> applications;

    /**
     * Combine this Config with another of the same type and returns a new Config.
     * <p>
     * The new config will have the same attributes as this config, with missing attributes supplied by other.
     * <p>
     * Sub components of the Config should be merged where possible.
     *
     * @param other The configuration to merge with.
     * @return A new Config which is a combination of this and other
     */
    @Override
    public IdolCustomApplicationsConfig merge(final IdolCustomApplicationsConfig other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    /**
     * Perform a basic validation of the internals of this Config.  This method should not rely on
     * external services.
     *
     * @param section the section to specify if a config exception is thrown if no section should be specified locally
     * @throws com.hp.autonomy.frontend.configuration.ConfigException If validation fails.
     */
    @Override
    public void basicValidate(final String section) throws ConfigException {
        for(final IdolCustomApplication app : applications) {
            if(StringUtils.isEmpty(app.getApplicationName())) {
                throw new ConfigException(section, "The application name must be a non-empty string, e.g. \"IDOL Admin\".");
            } else if(StringUtils.isEmpty(app.getUrl())) {
                throw new ConfigException(section, "The \"url\" property for \"" + app.getApplicationName() + "\" must not be empty.");
            } else {
                try {
                    new URL(app.getUrl());
                } catch(final MalformedURLException e) {
                    throw new ConfigException(section, "The URL provided for \"" + app.getApplicationName() + "\" is malformed. Cause: " + e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class IdolCustomApplicationsConfigBuilder {}
}
