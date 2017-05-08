/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

public interface WidgetDatasource<C extends WidgetDatasourceConfig<C>> {
    String getSource();

    C getConfig();
}
