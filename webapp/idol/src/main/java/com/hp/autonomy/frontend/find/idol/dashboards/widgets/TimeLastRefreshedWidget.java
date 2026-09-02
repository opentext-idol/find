package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@SuppressWarnings("WeakerAccess")
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(builder = TimeLastRefreshedWidget.TimeLastRefreshedWidgetBuilder.class)
public class TimeLastRefreshedWidget extends Widget<TimeLastRefreshedWidget, TimeLastRefreshedWidgetSettings> {
    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public TimeLastRefreshedWidget(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetNameSetting displayWidgetName, final TimeLastRefreshedWidgetSettings widgetSettings, final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, widgetSettings, cssClass);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TimeLastRefreshedWidgetBuilder {
    }
}
