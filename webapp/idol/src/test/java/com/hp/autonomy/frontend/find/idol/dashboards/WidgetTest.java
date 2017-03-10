/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class WidgetTest extends ConfigurationComponentTest<Widget> {

    @Override
    protected Class<Widget> getType() {
        return Widget.class;
    }

    @Override
    protected Widget constructComponent() {
        return Widget.builder()
                .name("Test Widget")
                .type("staticContentWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .savedSearch(new WidgetSearchId(123L, WidgetSearchId.Type.QUERY))
                .widgetSetting("content", "Hello World!")
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<Widget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "staticContentWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathNumberValue("$.savedSearch.id", 123)
                .hasJsonPathStringValue("$.savedSearch.type", "QUERY")
                .hasJsonPathStringValue("$.widgetSettings.content", "Hello World!");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<Widget> objectContent) {
        objectContent.assertThat().isEqualTo(
                Widget.builder()
                        .name("Default Widget")
                        .type("clockWidget")
                        .x(0)
                        .y(0)
                        .width(1)
                        .height(1)
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<Widget> objectContent) {
        objectContent.assertThat().isEqualTo(
                Widget.builder()
                        .name("Test Widget")
                        .type("staticContentWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .savedSearch(new WidgetSearchId(123L, WidgetSearchId.Type.QUERY))
                        .widgetSetting("content", "Hello World!")
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }
}
