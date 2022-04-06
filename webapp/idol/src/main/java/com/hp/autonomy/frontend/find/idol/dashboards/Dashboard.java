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

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.Widget;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = Dashboard.DashboardBuilder.class)
public class Dashboard extends SimpleComponent<Dashboard> implements OptionalConfigurationComponent<Dashboard> {
    private final String dashboardName;
    private final WidgetNameSetting displayWidgetNames;
    private final Boolean enabled;
    private final Integer width;
    private final Integer height;
    private final Integer updateInterval;
    private final Set<String> roles;

    @Singular
    private final Collection<Widget<?, ?>> widgets;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        super.basicValidate(section);

        if (width == null || width <= 0 || height == null || height <= 0) {
            throw new ConfigException("Dashboard Config", "Dashboard with name " + dashboardName + " does not have valid dimensions");
        }

        validateWidgets(section);
    }

    private void validateWidgets(final String section) throws ConfigException {
        final Map<Coordinate, Widget<?, ?>> gridCoordinates = new HashMap<>(width * height);
        for (final Widget<?, ?> widget : widgets) {
            widget.basicValidate(section);

            if (widget.getX() + widget.getWidth() > width || widget.getY() + widget.getHeight() > height) {
                throw new ConfigException(widget.getType(), "Widget of type " + widget.getType() + ", with name " + widget.getName() + ", extends outside the dashboard grid");
            }

            for (int x = widget.getX(); x < widget.getX() + widget.getWidth(); x++) {
                for (int y = widget.getY(); y < widget.getX() + widget.getWidth(); y++) {
                    final Coordinate coordinate = new Coordinate(x, y);
                    if (gridCoordinates.containsKey(coordinate)) {
                        final Widget<?, ?> otherWidget = gridCoordinates.get(coordinate);
                        throw new ConfigException(widget.getType(), "Coordinates of widget with name " + widget.getName() + " and type " + widget.getType()
                                + " overlap with those of widget with name " + otherWidget.getName() + " and type " + widget.getType());
                    } else {
                        gridCoordinates.put(coordinate, widget);
                    }
                }
            }
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class Coordinate {
        private final int x;
        private final int y;
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class DashboardBuilder {
    }
}
