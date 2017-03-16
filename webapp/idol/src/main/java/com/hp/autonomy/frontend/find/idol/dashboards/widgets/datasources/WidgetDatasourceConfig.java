package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

import com.hp.autonomy.frontend.configuration.ConfigurationComponent;

import java.util.Map;

public interface WidgetDatasourceConfig<C extends WidgetDatasourceConfig<C>> extends ConfigurationComponent<C> {
    Map<String, Object> getConfig();
}
