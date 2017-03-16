/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = WidgetDatasource.WidgetDatasourceBuilder.class)
public class WidgetDatasource extends SimpleComponent<WidgetDatasource> {
    private final Source source;
    private final Map<String, Object> config;

    public enum Source {
        savedsearch
    }

    public Object getConfigValue(final WidgetDatasourceConfigKey key) {
        return config.get(key.getValue());
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class WidgetDatasourceBuilder {}
}
