/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

import com.hp.autonomy.frontend.configuration.ConfigurationComponent;

import java.util.Map;

public interface WidgetDatasourceConfig<C extends WidgetDatasourceConfig<C>> extends ConfigurationComponent<C> {
    Map<String, Object> getConfig();
}
