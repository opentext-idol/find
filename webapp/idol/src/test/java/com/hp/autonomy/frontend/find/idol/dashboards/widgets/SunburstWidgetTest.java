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

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class SunburstWidgetTest extends ComplexWidgetTest<SunburstWidget, SunburstWidgetSettings> {
    @Override
    protected Class<SunburstWidget> getType() {
        return SunburstWidget.class;
    }

    @Override
    protected SunburstWidget constructComponent() {
        return SunburstWidget.builder()
                .name("Test Widget")
                .type("SunburstWidget")
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
                .widgetSettings(SunburstWidgetSettings.builder()
                        .firstField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                        .maxLegendEntries(5)
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/sunburstWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<SunburstWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "SunburstWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.datasource.config.id", 123)
                .hasJsonPathStringValue("$.datasource.config.type", "QUERY")
                .hasJsonPathStringValue("$.widgetSettings.firstField", "CONTENT_TYPE")
                .hasJsonPathNumberValue("$.widgetSettings.maxLegendEntries", 5);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<SunburstWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                SunburstWidget.builder()
                        .name("Sunburst Widget")
                        .type("SunburstWidget")
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
                        .widgetSettings(SunburstWidgetSettings.builder()
                                .firstField(tagNameFactory.buildTagName("CONTENT-TYPE"))
                                .secondField(tagNameFactory.buildTagName("AUTHOR"))
                                .maxLegendEntries(7)
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<SunburstWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                SunburstWidget.builder()
                        .name("Test Widget")
                        .type("SunburstWidget")
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
                        .widgetSettings(SunburstWidgetSettings.builder()
                                .firstField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                .secondField(tagNameFactory.buildTagName("AUTHOR"))
                                .maxLegendEntries(5)
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
    SunburstWidget constructComponentWithoutDatasource() {
        return SunburstWidget.builder()
                .name("Test Widget")
                .type("SunburstWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(SunburstWidgetSettings.builder()
                        .firstField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                        .maxLegendEntries(5)
                        .build())
                .build();
    }
}
