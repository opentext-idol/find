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

package com.hp.autonomy.frontend.find.core.configuration.export;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

public class PowerPointConfigTest extends ConfigurationComponentTest<PowerPointConfig> {
    @Test
    public void invalidMargin() throws Exception {
        try {
            PowerPointConfig.builder()
                .marginLeft(1D)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("All specified margin settings must be populated with values between 0 (inclusive) and 1 (exclusive)"));
        }
    }

    @Test
    public void invalidMarginDifference() throws Exception {
        try {
            PowerPointConfig.builder()
                .marginLeft(0.5)
                .marginRight(0.5)
                .build()
                .basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Top/bottom and left/right margin setting pairs should sum to less than 1"));
        }
    }

    @Test(expected = ConfigException.class)
    public void nonExistentTemplate() throws Exception {
        PowerPointConfig.builder()
            .templateFile("./customTemplate.pptx")
            .build()
            .basicValidate(null);
    }

    @Override
    protected Class<PowerPointConfig> getType() {
        return PowerPointConfig.class;
    }

    @Override
    protected PowerPointConfig constructComponent() {
        return PowerPointConfig.builder()
            .marginLeft(0.1)
            .marginRight(0.1)
            .marginTop(0.1)
            .marginBottom(0.1)
            .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return "{}";
    }

    @Override
    protected void validateJson(final JsonContent<PowerPointConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathNumberValue("@.marginLeft", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.marginRight", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.marginTop", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.marginBottom", 0.1);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<PowerPointConfig> objectContent) {
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginLeft", null);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginRight", null);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginTop", null);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginBottom", null);
        objectContent.assertThat().hasFieldOrPropertyWithValue("templateFile", null);
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<PowerPointConfig> objectContent) {
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginLeft", 0.1);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginRight", 0.1);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginTop", 0.1);
        objectContent.assertThat().hasFieldOrPropertyWithValue("marginBottom", 0.1);
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("marginLeft"));
    }
}
