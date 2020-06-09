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
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class StaticContentWidgetTest extends ConfigurationComponentTest<StaticContentWidget> {
    @Test
    public void noWidgetSettings() throws ConfigException {
        try {
            StaticContentWidget.builder()
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Widget Settings must be specified for Static Content Widget"));
        }
    }

    @Test
    public void noHtml() throws ConfigException {
        try {
            StaticContentWidget.builder()
                .widgetSettings(StaticContentWidgetSettings.builder().build())
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Static content widget must contain html"));
        }
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
