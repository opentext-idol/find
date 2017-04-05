/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(builder = TrendingWidget.TrendingWidgetBuilder.class)
public class TrendingWidget extends DatasourceDependentWidgetBase<TrendingWidget, TrendingWidgetSettings> {
    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public TrendingWidget(final String name,
                          final String type,
                          final Integer x,
                          final Integer y,
                          final Integer width,
                          final Integer height,
                          final WidgetDatasource<?> datasource,
                          final TrendingWidgetSettings widgetSettings) {
        super(name, type, x, y, width, height, datasource, widgetSettings);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TrendingWidgetBuilder {
    }
}
