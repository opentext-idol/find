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

public class ResultsListWidgetTest extends DatasourceDependentWidgetTest<ResultsListWidget, ResultsListWidgetSettings> {
    @Override
    protected Class<ResultsListWidget> getType() {
        return ResultsListWidget.class;
    }

    @Override
    protected ResultsListWidget constructComponent() {
        return ResultsListWidget.builder()
                .name("Test Widget")
                .type("ResultsListWidget")
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
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/resultsListWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<ResultsListWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "ResultsListWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<ResultsListWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                ResultsListWidget.builder()
                        .name("List")
                        .type("ResultsListWidget")
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
                        .widgetSettings(ResultsListWidgetSettings.builder()
                                .maxResults(6)
                                .sort("random")
                                .columnLayout(true)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<ResultsListWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                ResultsListWidget.builder()
                        .name("Test Widget")
                        .type("ResultsListWidget")
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
                        .widgetSettings(ResultsListWidgetSettings.builder()
                                .maxResults(6)
                                .sort("random")
                                .columnLayout(true)
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
    ResultsListWidget constructComponentWithoutDatasource() {
        return ResultsListWidget.builder()
                .name("Test Widget")
                .type("ResultsListWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .build();
    }
}
