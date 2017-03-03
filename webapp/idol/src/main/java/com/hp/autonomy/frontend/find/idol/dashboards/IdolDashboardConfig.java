/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = IdolDashboardConfig.IdolDashboardConfigBuilder.class)
public class IdolDashboardConfig extends AbstractConfig<IdolDashboardConfig> {

    @Singular
    private final Collection<Dashboard> dashboards;

    @Override
    public IdolDashboardConfig merge(final IdolDashboardConfig other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {}

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class IdolDashboardConfigBuilder {}
}
