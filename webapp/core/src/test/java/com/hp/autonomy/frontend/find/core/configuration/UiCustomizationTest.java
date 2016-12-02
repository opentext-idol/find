/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UiCustomizationTest extends ConfigurationComponentTest<UiCustomization> {
    @Override
    protected Class<UiCustomization> getType() {
        return UiCustomization.class;
    }

    @Override
    protected UiCustomization constructComponent() {
        final UiCustomizationRule rule = UiCustomizationRule.builder()
                .populateRule("user", false)
                .build();

        final UiCustomizationOptions uiCustomizationOptions = new UiCustomizationOptions();
        uiCustomizationOptions.populateRules("option3", rule);

        return UiCustomization.builder()
                .options(uiCustomizationOptions)
                .specialUrlPrefixes(ImmutableMap.of("application/vnd.visio", "ms-visio:ofv|u|"))
                .errorCallSupportString("Custom technical support message")
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization.json"));
    }

    @Override
    protected void validateJson(final JsonContent<UiCustomization> jsonContent) {
        jsonContent.assertThat().hasJsonPathBooleanValue("@.options.option3.user", false);
        jsonContent.assertThat().hasJsonPathStringValue("@.specialUrlPrefixes.['application/vnd.visio']", "ms-visio:ofv|u|");
        jsonContent.assertThat().hasJsonPathStringValue("@.errorCallSupportString", "Custom technical support message");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<UiCustomization> objectContent) {
        assertNotNull(objectContent.getObject().getOptions());
        assertFalse(objectContent.getObject().getOptions().any().isEmpty());
        assertThat(objectContent.getObject().getParametricNeverShow(), not(empty()));
        assertThat(objectContent.getObject().getParametricAlwaysShow(), not(empty()));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<UiCustomization> objectContent) {
        assertThat(objectContent.getObject().getOptions().any(), hasKey("option1"));
        assertThat(objectContent.getObject().getOptions().any(), hasKey("option2"));
        assertThat(objectContent.getObject().getOptions().any(), hasKey("option3"));
        assertThat(objectContent.getObject().getParametricNeverShow(), hasItem(is("A_CLEAN_NUMERIC_FIELD")));
        assertThat(objectContent.getObject().getParametricAlwaysShow(), hasItem(is("AUTN_DATE")));
        assertThat(objectContent.getObject().getSpecialUrlPrefixes(), hasKey("application/msword"));
        assertThat(objectContent.getObject().getSpecialUrlPrefixes(), hasKey("application/vnd.visio"));
    }

    @Override
    protected void validateString(final String s) {
    }
}
