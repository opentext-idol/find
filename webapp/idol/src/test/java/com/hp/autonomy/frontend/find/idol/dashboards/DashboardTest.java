/*
 * Copyright 2017 Hewlett Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class DashboardTest extends ConfigurationComponentTest<Dashboard> {
    @Override
    protected Class<Dashboard> getType() {
        return Dashboard.class;
    }

    @Override
    protected Dashboard constructComponent() {
        return Dashboard.builder()
                .dashboardName("My First Dashboard")
                .enabled(true)
                .width(5)
                .height(5)
                .widget(
                        Widget.builder()
                                .name("Sample Widget")
                                .build()
                )
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/dashboard.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<Dashboard> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.dashboardName", "My First Dashboard")
                .hasJsonPathBooleanValue("$.enabled", true)
                .hasJsonPathNumberValue("$.width", 5)
                .hasJsonPathNumberValue("$.height", 5)
                .doesNotHaveEmptyJsonPathValue("$.widgets");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<Dashboard> objectContent) {
        objectContent.assertThat().isEqualTo(
                Dashboard.builder()
                        .dashboardName("Default Dashboard")
                        .enabled(false)
                        .width(3)
                        .height(3)
                        .widgets(Collections.emptyList())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<Dashboard> objectContent) {
        objectContent.assertThat().isEqualTo(
                Dashboard.builder()
                        .dashboardName("My First Dashboard")
                        .enabled(true)
                        .width(5)
                        .height(5)
                        .widgets(
                                Collections.singletonList(
                                        Widget.builder()
                                                .name("Sample Widget")
                                                .build()
                                )
                        )
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("dashboardName"));
    }
}
