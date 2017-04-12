/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TrendingWidgetTest extends ComplexWidgetTest<TrendingWidget, TrendingWidgetSettings> {
    @Override
    protected Class<TrendingWidget> getType() {
        return TrendingWidget.class;
    }

    @Override
    protected TrendingWidget constructComponent() {
        return TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                        .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                        .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                        .maxValues(5)
                        .numberOfBuckets(12)
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/trendingWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<TrendingWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "TrendingWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY")
                .hasJsonPathStringValue("$.widgetSettings.parametricField", "/DOCUMENT/CONTENT_TYPE")
                .hasJsonPathStringValue("$.widgetSettings.dateField", "/DOCUMENT/AUTN_DATE")
                .hasJsonPathNumberValue("$.widgetSettings.maxValues", 5)
                .hasJsonPathNumberValue("$.widgetSettings.numberOfBuckets", 12);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TrendingWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TrendingWidget.builder()
                        .name("Test trending chart")
                        .type("TrendingWidget")
                        .x(0)
                        .y(4)
                        .width(5)
                        .height(2)
                        .datasource(SavedSearch.builder()
                                .source("SavedSearch")
                                .config(SavedSearchConfig.builder()
                                        .id(769L)
                                        .type(SavedSearchType.QUERY)
                                        .build())
                                .build())
                        .widgetSettings(TrendingWidgetSettings.builder()
                                .parametricField(tagNameFactory.buildTagName("OVERALL_VIBE"))
                                .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                .maxValues(7)
                                .numberOfBuckets(10)
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TrendingWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TrendingWidget.builder()
                        .name("Test Widget")
                        .type("TrendingWidget")
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
                        .widgetSettings(TrendingWidgetSettings.builder()
                                .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                .maxValues(5)
                                .numberOfBuckets(12)
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }

    @Override
    TrendingWidget constructComponentWithoutDatasource() {
        return TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(TrendingWidgetSettings.builder()
                        .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                        .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                        .maxValues(5)
                        .numberOfBuckets(12)
                        .build())
                .build();
    }
}
