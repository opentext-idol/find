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

import com.hp.autonomy.frontend.find.core.configuration.InitialLocation;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class MapWidgetTest extends DatasourceDependentWidgetTest<MapWidget, MapWidgetSettings> {
    @Override
    protected Class<MapWidget> getType() {
        return MapWidget.class;
    }

    @Override
    protected MapWidget constructComponent() {
        return MapWidget.builder()
                .name("Test Widget")
                .type("MapWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .datasource(SavedSearch.builder()
                        .source("SavedSearch")
                        .config(SavedSearchConfig.builder()
                                .id(123L)
                                .type(SavedSearchType.QUERY)
                                .build())
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/mapWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<MapWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "MapWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<MapWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                MapWidget.builder()
                        .name("Map")
                        .type("MapWidget")
                        .x(1)
                        .y(1)
                        .width(2)
                        .height(2)
                        .datasource(SavedSearch.builder()
                                .source("SavedSearch")
                                .config(SavedSearchConfig.builder()
                                        .id(290L)
                                        .type(SavedSearchType.SNAPSHOT)
                                        .build())
                                .build())
                        .widgetSettings(MapWidgetSettings.builder()
                                .maxResults(1000)
                                .locationFieldPairs(Arrays.asList("DefaultLocation", "Secret Lair"))
                                .centerCoordinates(new InitialLocation(51.5, 0.12))
                                .zoomLevel(3)
                                .clusterMarkers(true)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<MapWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                MapWidget.builder()
                        .name("Test Widget")
                        .type("MapWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .datasource(SavedSearch.builder()
                                .source("SavedSearch")
                                .config(SavedSearchConfig.builder()
                                        .id(123L)
                                        .type(SavedSearchType.QUERY)
                                        .build())
                                .build())
                        .widgetSettings(MapWidgetSettings.builder()
                                .maxResults(1000)
                                .locationFieldPairs(Arrays.asList("DefaultLocation", "Secret Lair"))
                                .centerCoordinates(new InitialLocation(51.5, 0.12))
                                .zoomLevel(3)
                                .clusterMarkers(true)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }

    @Override
    MapWidget constructComponentWithoutDatasource() {
        return MapWidget.builder()
                .name("Test Widget")
                .type("MapWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .build();
    }
}
