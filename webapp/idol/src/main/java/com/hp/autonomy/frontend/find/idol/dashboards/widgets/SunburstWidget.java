/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(builder = SunburstWidget.SunburstWidgetBuilder.class)
public class SunburstWidget extends DatasourceDependentWidgetBase<SunburstWidget, SunburstWidgetSettings> {
    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public SunburstWidget(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetNameSetting displayWidgetName, final WidgetDatasource<?> datasource, final SunburstWidgetSettings widgetSettings, final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, datasource, widgetSettings, cssClass);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class SunburstWidgetBuilder {
    }
}
