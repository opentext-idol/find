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
@JsonDeserialize(builder = TimeLastRefreshedWidget.TimeLastRefreshedWidgetBuilder.class)
public class TimeLastRefreshedWidget extends Widget<TimeLastRefreshedWidget, TimeLastRefreshedWidgetSettings> {
    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public TimeLastRefreshedWidget(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetDatasource<?, ?> datasource, final TimeLastRefreshedWidgetSettings widgetSettings) {
        super(name, type, x, y, width, height, datasource, widgetSettings);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TimeLastRefreshedWidgetBuilder {
    }
}
