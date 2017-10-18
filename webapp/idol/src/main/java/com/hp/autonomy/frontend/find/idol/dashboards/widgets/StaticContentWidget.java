package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(builder = StaticContentWidget.StaticContentWidgetBuilder.class)
public class StaticContentWidget extends Widget<StaticContentWidget, StaticContentWidgetSettings> {
    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Builder(toBuilder = true)
    public StaticContentWidget(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetNameSetting displayWidgetName, final StaticContentWidgetSettings widgetSettings, final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, widgetSettings, cssClass);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (widgetSettings == null) {
            throw new ConfigException("Static Content Widget", "Widget Settings must be specified for Static Content Widget");
        }

        super.basicValidate(section);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class StaticContentWidgetBuilder {
    }
}
