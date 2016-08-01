/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

public class UiCustomizationRuleTest extends ConfigurationComponentTest<UiCustomizationRule> {
    @Override
    protected UiCustomizationRule constructComponent() {
        final UiCustomizationRule rule = new UiCustomizationRule();
        rule.populateRule("user", false);

        return rule;
    }

    @Override
    protected Class<UiCustomizationRule> getComponentType() {
        return UiCustomizationRule.class;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationRuleTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization-rule.json"));
    }

    @Override
    protected void validateJson(final String json) {
        assertTrue(json.contains("user"));
        assertTrue(json.contains("false"));
    }

    @Override
    protected void validateParsedComponent(final UiCustomizationRule component) {
        final Map<String, Object> rules = component.any();
        assertThat(rules, hasKey("user"));
        assertThat(rules, hasKey("bi"));
    }

    @Override
    protected void validateMergedComponent(final UiCustomizationRule mergedComponent) {
        final Map<String, Object> rules = mergedComponent.any();
        assertThat(rules, Matchers.<String, Object>hasEntry("user", false));
        assertThat(rules, Matchers.<String, Object>hasEntry("bi", true));
    }
}
