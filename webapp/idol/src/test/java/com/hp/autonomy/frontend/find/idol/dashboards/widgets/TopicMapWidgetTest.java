/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.core.ResolvableType;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class TopicMapWidgetTest extends ConfigurationComponentTest<TopicMapWidget> {
    @Override
    public void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class);
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Override
    protected Class<TopicMapWidget> getType() {
        return TopicMapWidget.class;
    }

    @Override
    protected TopicMapWidget constructComponent() {
        return TopicMapWidget.builder()
                .name("Test Widget")
                .type("TopicMapWidget")
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
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/topicMapWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<TopicMapWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "TopicMapWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TopicMapWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TopicMapWidget.builder()
                        .name("TopicMap")
                        .type("TopicMapWidget")
                        .x(1)
                        .y(1)
                        .width(2)
                        .height(2)
                        .datasource(SavedSearch.builder()
                                .source("SavedSearch")
                                .config(SavedSearchConfig.builder()
                                        .id(97L)
                                        .type(SavedSearchType.QUERY)
                                        .build())
                                .build())
                        .widgetSettings(TopicMapWidgetSettings.builder()
                                .maxResults(1000)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TopicMapWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TopicMapWidget.builder()
                        .name("Test Widget")
                        .type("TopicMapWidget")
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
                        .widgetSettings(TopicMapWidgetSettings.builder()
                                .maxResults(1000)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }
}
