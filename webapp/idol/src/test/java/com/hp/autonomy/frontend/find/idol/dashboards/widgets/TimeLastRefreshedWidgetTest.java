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

public class TimeLastRefreshedWidgetTest extends ConfigurationComponentTest<TimeLastRefreshedWidget> {
    @Override
    protected Class<TimeLastRefreshedWidget> getType() {
        return TimeLastRefreshedWidget.class;
    }

    @Override
    protected TimeLastRefreshedWidget constructComponent() {
        return TimeLastRefreshedWidget.builder()
                .name("Test Widget")
                .type("TimeLastRefreshedWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(TimeLastRefreshedWidgetSettings.builder()
                        .dateFormat("HH:mm z")
                        .timeZone("Europe/London")
                        .widgetSetting("testing", "testing")
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/timeLastRefreshedWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<TimeLastRefreshedWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "TimeLastRefreshedWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathStringValue("$.widgetSettings.dateFormat", "HH:mm z")
                .hasJsonPathStringValue("$.widgetSettings.timeZone", "Europe/London")
                .hasJsonPathStringValue("$.widgetSettings.testing", "testing");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TimeLastRefreshedWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TimeLastRefreshedWidget.builder()
                        .name("Time Last Refreshed")
                        .type("TimeLastRefreshedWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TimeLastRefreshedWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                TimeLastRefreshedWidget.builder()
                        .name("Test Widget")
                        .type("TimeLastRefreshedWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .widgetSettings(TimeLastRefreshedWidgetSettings.builder()
                                .dateFormat("HH:mm z")
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
