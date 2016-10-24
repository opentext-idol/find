/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UiCustomizationTest extends ConfigurationComponentTest<UiCustomization> {
    @Override
    protected UiCustomization constructComponent() {
        final UiCustomizationRule rule = new UiCustomizationRule();
        rule.populateRule("user", false);

        final UiCustomizationOptions uiCustomizationOptions = new UiCustomizationOptions();
        uiCustomizationOptions.populateRules("option3", rule);

        return UiCustomization.builder()
                .setOptions(uiCustomizationOptions)
                .setSpecialUrlPrefixes(ImmutableMap.of("application/vnd.visio", "ms-visio:ofv|u|"))
                .setErrorCallSupportString("Custom technical support message")
                .build();
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
        assertTrue(json.contains("application/vnd.visio"));
        assertTrue(json.contains("ms-visio:ofv|u|"));
        assertTrue(json.contains("Custom technical support message"));
    }

    @Override
    protected void validateParsedComponent(final UiCustomization component) {
        assertNotNull(component.getOptions());
        assertFalse(component.getOptions().any().isEmpty());
        assertThat(component.getParametricNeverShow(), not(empty()));
        assertThat(component.getParametricAlwaysShow(), not(empty()));
    }

    @Override
    protected void validateMergedComponent(final UiCustomization mergedComponent) {
        assertThat(mergedComponent.getOptions().any(), hasKey("option1"));
        assertThat(mergedComponent.getOptions().any(), hasKey("option2"));
        assertThat(mergedComponent.getOptions().any(), hasKey("option3"));
        assertThat(mergedComponent.getParametricNeverShow(), hasItem(is("A_CLEAN_NUMERIC_FIELD")));
        assertThat(mergedComponent.getParametricAlwaysShow(), hasItem(is("AUTN_DATE")));
        assertThat(mergedComponent.getSpecialUrlPrefixes(), hasKey("application/msword"));
        assertThat(mergedComponent.getSpecialUrlPrefixes(), hasKey("application/vnd.visio"));
    }
}
