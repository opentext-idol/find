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

import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleWidgetTest extends ConfigurationComponentTest<SimpleWidget> {

    @Override
    protected Class<SimpleWidget> getType() {
        return SimpleWidget.class;
    }

    @Override
    protected SimpleWidget constructComponent() {
        return SimpleWidget.builder()
                .name("Test Widget")
                .type("StaticContentWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .displayWidgetName(WidgetNameSetting.always)
                .datasource(SavedSearch.builder()
                        .source("SavedSearch")
                        .config(SavedSearchConfig.builder()
                                .id(123L)
                                .type(SavedSearchType.QUERY)
                                .build())
                        .build())
                .widgetSettings(SimpleWidgetSettings.builder()
                        .widgetSetting("content", "Hello World!")
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/widget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<SimpleWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "StaticContentWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathStringValue("$.displayWidgetName", "always")
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY")
                .hasJsonPathStringValue("$.widgetSettings.content", "Hello World!");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<SimpleWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                SimpleWidget.builder()
                        .name("Default Widget")
                        .type("ClockWidget")
                        .x(0)
                        .y(0)
                        .width(1)
                        .height(1)
                        .displayWidgetName(WidgetNameSetting.never)
                        .widgetSettings(SimpleWidgetSettings.builder().build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<SimpleWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                SimpleWidget.builder()
                        .name("Test Widget")
                        .type("StaticContentWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .displayWidgetName(WidgetNameSetting.always)
                        .datasource(SavedSearch.builder()
                                .source("SavedSearch")
                                .config(SavedSearchConfig.builder()
                                        .id(123L)
                                        .type(SavedSearchType.QUERY)
                                        .build())
                                .build())
                        .widgetSettings(SimpleWidgetSettings.builder()
                                .widgetSetting("content", "Hello World!")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }
}
