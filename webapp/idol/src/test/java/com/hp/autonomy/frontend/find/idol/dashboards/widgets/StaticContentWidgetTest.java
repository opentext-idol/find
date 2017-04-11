/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticContentWidgetTest extends ConfigurationComponentTest<StaticContentWidget> {
    @Test(expected = ConfigException.class)
    public void noWidgetSettings() throws ConfigException {
        StaticContentWidget.builder()
                .build()
                .basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void noHtml() throws ConfigException {
        StaticContentWidget.builder()
                .widgetSettings(StaticContentWidgetSettings.builder()
                        .build())
                .build()
                .basicValidate(null);
    }

    @Override
    protected Class<StaticContentWidget> getType() {
        return StaticContentWidget.class;
    }

    @Override
    protected StaticContentWidget constructComponent() {
        return StaticContentWidget.builder()
                .name("Test Widget")
                .type("StaticContentWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(StaticContentWidgetSettings.builder()
                        .html("Hello World!")
                        .widgetSetting("testing", "testing")
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/staticContentWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<StaticContentWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "StaticContentWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathStringValue("$.widgetSettings.html", "Hello World!")
                .hasJsonPathStringValue("$.widgetSettings.testing", "testing");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<StaticContentWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                StaticContentWidget.builder()
                        .name("")
                        .type("StaticContentWidget")
                        .x(1)
                        .y(1)
                        .width(2)
                        .height(2)
                        .widgetSettings(StaticContentWidgetSettings.builder()
                                .html("<div><p style=\"font-weight: bold; font-style: italic\">I love cheese</p><p>cheese is the best</p></div>")
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<StaticContentWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                StaticContentWidget.builder()
                        .name("Test Widget")
                        .type("StaticContentWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .widgetSettings(StaticContentWidgetSettings.builder()
                                .html("Hello World!")
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
