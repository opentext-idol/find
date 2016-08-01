/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

public class UiCustomizationOptionsTest extends ConfigurationComponentTest<UiCustomizationOptions> {
    @Override
    protected UiCustomizationOptions constructComponent() {
        final UiCustomizationRule rule1 = new UiCustomizationRule();
        rule1.populateRule("user", false);

        final UiCustomizationRule rule2 = new UiCustomizationRule();
        rule2.populateRule("user", true);
        rule2.populateRule("bi", false);

        final UiCustomizationOptions uiCustomizationOptions = new UiCustomizationOptions();
        uiCustomizationOptions.populateRules("option2", rule1);
        uiCustomizationOptions.populateRules("option3", rule2);
        return uiCustomizationOptions;
    }

    @Override
    protected Class<UiCustomizationOptions> getComponentType() {
        return UiCustomizationOptions.class;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization-options.json"));
    }

    @Override
    protected void validateJson(final String json) {
        assertTrue(json.contains("option2"));
        assertTrue(json.contains("option3"));
        assertTrue(json.contains("user"));
        assertTrue(json.contains("bi"));
        assertTrue(json.contains("true"));
        assertTrue(json.contains("false"));
    }

    @Override
    protected void validateParsedComponent(final UiCustomizationOptions component) {
        final Map<String, UiCustomizationRule> rules = component.any();
        assertThat(rules, hasKey("option1"));
        assertThat(rules, hasKey("option2"));
    }

    @Override
    protected void validateMergedComponent(final UiCustomizationOptions mergedComponent) {
        final Map<String, UiCustomizationRule> rules = mergedComponent.any();
        assertThat(rules, hasKey("option1"));
        assertThat(rules, hasKey("option2"));
        assertThat(rules, hasKey("option3"));
    }
}
