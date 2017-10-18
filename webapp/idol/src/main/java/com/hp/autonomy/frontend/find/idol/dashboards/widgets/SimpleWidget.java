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
@JsonDeserialize(builder = SimpleWidget.SimpleWidgetBuilder.class)
public class SimpleWidget extends Widget<SimpleWidget, SimpleWidgetSettings> implements DatasourceDependentWidget {
    private final WidgetDatasource<?> datasource;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public SimpleWidget(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetNameSetting displayWidgetName, final WidgetDatasource<?> datasource, final SimpleWidgetSettings widgetSettings, final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, widgetSettings, cssClass);
        this.datasource = datasource;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class SimpleWidgetBuilder {
    }
}
