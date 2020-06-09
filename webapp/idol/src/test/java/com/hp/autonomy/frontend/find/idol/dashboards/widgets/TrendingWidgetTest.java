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
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TrendingWidgetTest extends ComplexWidgetTest<TrendingWidget, TrendingWidgetSettings> {
    @Test
    public void parametricFieldMissing() throws ConfigException {
        try {
            TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                                    .build())
                .build()
                .basicValidate(null);
            Assert.fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Trending widget config must specify a parametric field"));
        }
    }

    @Test
    public void invalidColor() throws ConfigException {
        try {
            TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                                    .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                    .values(Arrays.asList(new TrendingWidgetSettings.TrendingValue("POSITIVE", "green"),
                                                          new TrendingWidgetSettings.TrendingValue("NEGATIVE", "cucumber")))
                                    .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       allOf(containsString("The following parametric values have invalid colours associated"),
                             containsString("name=NEGATIVE"),
                             containsString("color=cucumber"),
                             not(containsString("green"))));
        }
    }

    @Test
    public void invalidDates() throws ConfigException {
        try {
            TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                                    .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                    .minDate(ZonedDateTime.parse("2010-04-05T00:00:00Z"))
                                    .maxDate(ZonedDateTime.parse("2009-04-05T00:00:00Z"))
                                    .values(Arrays.asList(new TrendingWidgetSettings.TrendingValue("POSITIVE", "green"),
                                                          new TrendingWidgetSettings.TrendingValue("NEGATIVE", "blue")))
                                    .build())
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Invalid date range. Configured min date is greater than max date"));
        }
    }

    @Override
    protected Class<TrendingWidget> getType() {
        return TrendingWidget.class;
    }

    @Override
    protected TrendingWidget constructComponent() {
        return TrendingWidget.builder()
            .name("Test Widget")
            .type("TrendingWidget")
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
            .widgetSettings(TrendingWidgetSettings.builder()
                                .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                .maxValues(5)
                                .numberOfBuckets(12)
                                .minDate(ZonedDateTime.parse("2009-04-05T00:00:00Z"))
                                .maxDate(ZonedDateTime.parse("2010-04-05T00:00:00Z"))
                                .build())
            .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
            getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/trendingWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<TrendingWidget> jsonContent) {
        jsonContent.assertThat()
            .hasJsonPathStringValue("$.name", "Test Widget")
            .hasJsonPathStringValue("$.type", "TrendingWidget")
            .hasJsonPathNumberValue("$.x", 1)
            .hasJsonPathNumberValue("$.y", 1)
            .hasJsonPathNumberValue("$.width", 1)
            .hasJsonPathNumberValue("$.height", 1)
            .hasJsonPathNumberValue("$.datasource.config.id", 123)
            .hasJsonPathStringValue("$.datasource.config.type", "QUERY")
            .hasJsonPathStringValue("$.widgetSettings.parametricField", "/DOCUMENT/CONTENT_TYPE")
            .hasJsonPathStringValue("$.widgetSettings.dateField", "/DOCUMENT/AUTN_DATE")
            .hasJsonPathNumberValue("$.widgetSettings.maxValues", 5)
            .hasJsonPathStringValue("$.widgetSettings.maxDate", "2010-04-05T00:00:00Z")
            .hasJsonPathStringValue("$.widgetSettings.minDate", "2009-04-05T00:00:00Z")
            .hasJsonPathNumberValue("$.widgetSettings.numberOfBuckets", 12);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TrendingWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
            TrendingWidget.builder()
                .name("Test trending chart")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                                    .parametricField(tagNameFactory.buildTagName("OVERALL_VIBE"))
                                    .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                    .maxValues(7)
                                    .numberOfBuckets(10)
                                    .minDate(ZonedDateTime.parse("2009-04-05T00:00:00Z[UTC]"))
                                    .maxDate(ZonedDateTime.parse("2010-04-05T00:00:00Z[UTC]"))
                                    .values(Arrays.asList(new TrendingWidgetSettings.TrendingValue("POSITIVE", "green"),
                                                          new TrendingWidgetSettings.TrendingValue("NEGATIVE", "red")))
                                    .build())
                .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TrendingWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
            TrendingWidget.builder()
                .name("Test Widget")
                .type("TrendingWidget")
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
                .widgetSettings(TrendingWidgetSettings.builder()
                                    .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                    .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                    .maxValues(5)
                                    .numberOfBuckets(12)
                                    .minDate(ZonedDateTime.parse("2009-04-05T00:00:00Z"))
                                    .maxDate(ZonedDateTime.parse("2010-04-05T00:00:00Z"))
                                    .values(Arrays.asList(new TrendingWidgetSettings.TrendingValue("POSITIVE", "green"),
                                                          new TrendingWidgetSettings.TrendingValue("NEGATIVE", "red")))
                                    .build())
                .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }

    @Override
    TrendingWidget constructComponentWithoutDatasource() {
        return TrendingWidget.builder()
            .name("Test Widget")
            .type("TrendingWidget")
            .x(1)
            .y(1)
            .width(1)
            .height(1)
            .widgetSettings(TrendingWidgetSettings.builder()
                                .parametricField(tagNameFactory.buildTagName("CONTENT_TYPE"))
                                .dateField(tagNameFactory.buildTagName("AUTN_DATE"))
                                .maxValues(5)
                                .numberOfBuckets(12)
                                .build())
            .build();
    }

    // Jackson's serialization and deserialization of the max and min dates is not symmetrical.
    @Override
    public void jsonSymmetry() throws IOException {}
}
