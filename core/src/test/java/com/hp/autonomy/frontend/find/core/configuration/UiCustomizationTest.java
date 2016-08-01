/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.*;

public class UiCustomizationTest extends ConfigurationComponentTest<UiCustomization> {
    @Override
    protected UiCustomization constructComponent() {
        final UiCustomizationRule rule = new UiCustomizationRule();
        rule.populateRule("user", false);

        final UiCustomizationOptions uiCustomizationOptions = new UiCustomizationOptions();
        uiCustomizationOptions.populateRules("option3", rule);

        return new UiCustomization(uiCustomizationOptions);
    }

    @Override
    protected Class<UiCustomization> getComponentType() {
        return UiCustomization.class;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization.json"));
    }

    @Override
    protected void validateJson(final String json) {
        assertTrue(json.contains("options"));
        assertTrue(json.contains("option3"));
    }

    @Override
    protected void validateParsedComponent(final UiCustomization component) {
        assertNotNull(component.getOptions());
        assertFalse(component.getOptions().any().isEmpty());
    }

    @Override
    protected void validateMergedComponent(final UiCustomization mergedComponent) {
        assertThat(mergedComponent.getOptions().any(), hasKey("option1"));
        assertThat(mergedComponent.getOptions().any(), hasKey("option2"));
        assertThat(mergedComponent.getOptions().any(), hasKey("option3"));
    }
}
