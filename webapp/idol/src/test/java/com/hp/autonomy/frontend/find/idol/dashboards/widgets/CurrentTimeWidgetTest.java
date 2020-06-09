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
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class CurrentTimeWidgetTest extends ConfigurationComponentTest<CurrentTimeWidget> {

    @Override
    protected Class<CurrentTimeWidget> getType() {
        return CurrentTimeWidget.class;
    }

    @Override
    protected CurrentTimeWidget constructComponent() {
        return CurrentTimeWidget.builder()
                .name("Test Widget")
                .type("CurrentTimeWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(CurrentTimeWidgetSettings.builder()
                        .dateFormat("ll")
                        .timeFormat("HH:mm z")
                        .timeZone("Europe/London")
                        .widgetSetting("testing", "testing")
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/currentTimeWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<CurrentTimeWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "CurrentTimeWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathStringValue("$.widgetSettings.dateFormat", "ll")
                .hasJsonPathStringValue("$.widgetSettings.timeFormat", "HH:mm z")
                .hasJsonPathStringValue("$.widgetSettings.timeZone", "Europe/London")
                .hasJsonPathStringValue("$.widgetSettings.testing", "testing");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<CurrentTimeWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                CurrentTimeWidget.builder()
                        .name("Current Time Date")
                        .type("CurrentTimeWidget")
                        .x(1)
                        .y(1)
                        .width(2)
                        .height(2)
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<CurrentTimeWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                CurrentTimeWidget.builder()
                        .name("Test Widget")
                        .type("CurrentTimeWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .widgetSettings(CurrentTimeWidgetSettings.builder()
                                .dateFormat("ll")
                                .timeFormat("HH:mm z")
                                .timeZone("Europe/London")
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
