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

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TrendingConfigurationTest extends ConfigurationComponentTest<TrendingConfiguration> {
    @Autowired
    private FieldPathNormaliser fieldPathNormaliser;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setUp() {
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Test
    public void badDateField() throws ConfigException {
        try {
            TrendingConfiguration.builder()
                .dateField(fieldPathNormaliser.normaliseFieldPath(""))
                .numberOfValues(10)
                .defaultNumberOfBuckets(15)
                .maxNumberOfBuckets(20)
                .minNumberOfBuckets(10)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("dateField must be provided"));
        }
    }

    @Test
    public void badNumberOfValues() throws ConfigException {
        try {
            TrendingConfiguration.builder()
                .dateField(fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
                .numberOfValues(0)
                .defaultNumberOfBuckets(15)
                .maxNumberOfBuckets(20)
                .minNumberOfBuckets(10)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Number of values must be provided and must be greater than 0"));
        }
    }

    @Test
    public void badNumberOfBuckets() throws ConfigException {
        try {
            TrendingConfiguration.builder()
                .dateField(fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
                .numberOfValues(10)
                .defaultNumberOfBuckets(15)
                .maxNumberOfBuckets(20)
                .minNumberOfBuckets(0)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Minimum number of buckets must be provided and must be greater than 0"));
        }
    }

    @Test
    public void nonsensicalBucketingConfiguration() throws ConfigException {
        try {
            TrendingConfiguration.builder()
                .dateField(fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
                .numberOfValues(10)
                .defaultNumberOfBuckets(20)
                .maxNumberOfBuckets(10)
                .minNumberOfBuckets(15)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Default number of buckets must lie between max and min"));
        }
    }

    @Override
    protected Class<TrendingConfiguration> getType() {
        return TrendingConfiguration.class;
    }

    @Override
    protected TrendingConfiguration constructComponent() {
        return TrendingConfiguration.builder()
            .dateField(fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
            .numberOfValues(10)
            .defaultNumberOfBuckets(15)
            .maxNumberOfBuckets(20)
            .minNumberOfBuckets(10)
            .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/trending.json"));
    }

    @Override
    protected void validateJson(final JsonContent<TrendingConfiguration> jsonContent) {
        jsonContent.assertThat()
            .hasJsonPathStringValue("$.dateField", ParametricValuesService.AUTN_DATE_FIELD)
            .hasJsonPathNumberValue("$.numberOfValues", 10)
            .hasJsonPathNumberValue("$.maxNumberOfBuckets", 20)
            .hasJsonPathNumberValue("$.minNumberOfBuckets", 10)
            .hasJsonPathNumberValue("$.defaultNumberOfBuckets", 15);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TrendingConfiguration> objectContent) {
        objectContent.assertThat()
            .hasFieldOrPropertyWithValue("dateField", fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
            .hasFieldOrPropertyWithValue("numberOfValues", 10)
            .hasFieldOrPropertyWithValue("maxNumberOfBuckets", 20)
            .hasFieldOrPropertyWithValue("minNumberOfBuckets", 10)
            .hasFieldOrPropertyWithValue("defaultNumberOfBuckets", 20);
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TrendingConfiguration> objectContent) {
        objectContent.assertThat()
            .hasFieldOrPropertyWithValue("dateField", fieldPathNormaliser.normaliseFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
            .hasFieldOrPropertyWithValue("numberOfValues", 10)
            .hasFieldOrPropertyWithValue("maxNumberOfBuckets", 20)
            .hasFieldOrPropertyWithValue("minNumberOfBuckets", 10)
            .hasFieldOrPropertyWithValue("defaultNumberOfBuckets", 15);
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("dateField"));
    }
}
