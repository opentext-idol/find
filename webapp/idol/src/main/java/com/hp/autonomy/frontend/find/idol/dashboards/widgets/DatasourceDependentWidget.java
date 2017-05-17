package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;

@FunctionalInterface
public interface DatasourceDependentWidget {
    WidgetDatasource<?> getDatasource();
}
