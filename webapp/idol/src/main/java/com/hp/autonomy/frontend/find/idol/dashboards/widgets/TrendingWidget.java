/*
 * Copyright 2017 Open Text.
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

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@Getter
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
                          final WidgetNameSetting displayWidgetName,
                          final WidgetDatasource<?> datasource,
                          final TrendingWidgetSettings widgetSettings,
                          final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, datasource, widgetSettings, cssClass);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TrendingWidgetBuilder {
    }
}
