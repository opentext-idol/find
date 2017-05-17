package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigurationComponent;

import java.util.Map;

/**
 * Widget-specific settings
 */
public interface WidgetSettings<WS extends WidgetSettings<WS>> extends ConfigurationComponent<WS> {
    Map<String, Object> getWidgetSettings();
}
