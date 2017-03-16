/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class WidgetDatasource<W extends WidgetDatasource<W, C>, C extends WidgetDatasourceConfig<C>> extends SimpleComponent<WidgetDatasource<W, C>> {
    protected final String source;
    protected final C config;
}
