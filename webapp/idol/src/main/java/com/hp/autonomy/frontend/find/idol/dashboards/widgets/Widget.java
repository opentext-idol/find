/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class Widget<W extends Widget<W, WS>, WS extends WidgetSettings<WS>> extends SimpleComponent<W> {
    protected final String name;
    protected final String type;
    protected final Integer x;
    protected final Integer y;
    protected final Integer width;
    protected final Integer height;
    protected final WS widgetSettings;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (x == null || y == null || width == null || height == null) {
            throw new ConfigException(type, "widget with name " + name + " and type " + type + " does not have valid coordinates and dimensions");
        }

        super.basicValidate(section);
    }
}
