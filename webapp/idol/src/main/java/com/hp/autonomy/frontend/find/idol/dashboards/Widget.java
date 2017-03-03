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
import lombok.Singular;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = Widget.WidgetBuilder.class)
public class Widget extends SimpleComponent<Widget> {
    private final String name;
    private final String type;
    private final Integer x;
    private final Integer y;
    private final Integer width;
    private final Integer height;
    private final WidgetSearchId savedSearch;

    @Singular
    private final Map<String, Object> widgetSettings;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class WidgetBuilder {}
}
