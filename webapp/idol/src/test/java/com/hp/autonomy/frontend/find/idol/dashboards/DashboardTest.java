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

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.SimpleWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.SimpleWidgetSettings;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.SunburstWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.SunburstWidgetSettings;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.TagNameSerializer;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.Widget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.WidgetMixins;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public class DashboardTest extends ConfigurationComponentTest<Dashboard> {
    @Autowired
    private TagNameFactory tagNameFactory;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setUp() {
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(TagName.class, new TagNameSerializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.addMixIn(Widget.class, WidgetMixins.class);
        objectMapper.addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class);
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Test
    public void dashboardWithoutCoordinates() throws ConfigException {
        final String dashboardName = "My First Dashboard";
        try {
            Dashboard.builder()
                .dashboardName(dashboardName)
                .enabled(true)
                .widget(SimpleWidget.builder()
                            .name("Sample Widget")
                            .x(1)
                            .y(1)
                            .width(1)
                            .height(1)
                            .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       allOf(containsString(dashboardName),
                             containsString("does not have valid dimensions")));
        }
    }

    @Test
    public void widgetWithoutCoordinates() throws ConfigException {
        final String widgetName = "Sample Widget";
        try {
            Dashboard.builder()
                .dashboardName("My First Dashboard")
                .enabled(true)
                .width(5)
                .height(5)
                .widget(SimpleWidget.builder()
                            .name(widgetName)
                            .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       allOf(containsString(widgetName),
                             containsString("does not have valid coordinates and dimensions")));
        }
    }

    @Test
    public void widgetOutsideBounds() throws ConfigException {
        final String widgetName = "Sample Widget";
        try {
            Dashboard.builder()
                .dashboardName("My First Dashboard")
                .enabled(true)
                .width(5)
                .height(5)
                .widget(SimpleWidget.builder()
                            .name(widgetName)
                            .x(1)
                            .y(1)
                            .width(4)
                            .height(5)
                            .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       allOf(containsString(widgetName),
                             containsString("extends outside the dashboard grid")));
        }
    }

    @Test
    public void overlappingWidgets() throws ConfigException {
        final String firstWidgetName = "First Widget";
        final String secondWidgetName = "Second Widget";
        try {
            Dashboard.builder()
                .dashboardName("My First Dashboard")
                .enabled(true)
                .width(5)
                .height(5)
                .widget(SimpleWidget.builder()
                            .name(firstWidgetName)
                            .x(0)
                            .y(0)
                            .width(2)
                            .height(2)
                            .build())
                .widget(SimpleWidget.builder()
                            .name(secondWidgetName)
                            .x(1)
                            .y(1)
                            .width(2)
                            .height(2)
                            .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       allOf(containsString(firstWidgetName),
                             containsString(secondWidgetName),
                             containsString("overlap")));
        }
    }

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
            .widget(SimpleWidget.builder()
                        .name("Sample Widget")
                        .x(0)
                        .y(0)
                        .width(1)
                        .height(1)
                        .build())
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
                .widget(SimpleWidget.builder()
                            .name("Default Widget")
                            .type("ClockWidget")
                            .x(0)
                            .y(0)
                            .width(1)
                            .height(1)
                            .widgetSettings(SimpleWidgetSettings.builder().build())
                            .build())
                .widget(SunburstWidget.builder()
                            .name("star 769 (content type/author) 7 entries")
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
                                                .build())
                            .build())
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
                .widget(SimpleWidget.builder()
                            .name("Sample Widget")
                            .x(0)
                            .y(0)
                            .width(1)
                            .height(1)
                            .build())
                .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("dashboardName"));
    }
}
