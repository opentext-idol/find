/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
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
    protected final WidgetNameSetting displayWidgetName;
    protected final WS widgetSettings;
    protected final String cssClass;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (x == null || x < 0 || y == null || y < 0 || width == null || width <= 0 || height == null || height <= 0) {
            throw new ConfigException(type, "Widget with name " + name + " and type " + type + " does not have valid coordinates and dimensions");
        }

        super.basicValidate(section);
    }
}
