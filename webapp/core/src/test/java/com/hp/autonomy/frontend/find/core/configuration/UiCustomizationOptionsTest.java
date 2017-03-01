/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

public class UiCustomizationOptionsTest extends ConfigurationComponentTest<UiCustomizationOptions> {
    @Override
    protected Class<UiCustomizationOptions> getType() {
        return UiCustomizationOptions.class;
    }

    @Override
    protected UiCustomizationOptions constructComponent() {
        final UiCustomizationRule rule1 = UiCustomizationRule.builder()
                .populateRule("user", false)
                .build();

        final UiCustomizationRule rule2 = UiCustomizationRule.builder()
                .populateRule("user", true)
                .populateRule("bi", false)
                .build();

        final UiCustomizationOptions uiCustomizationOptions = new UiCustomizationOptions();
        uiCustomizationOptions.populateRules("option2", rule1);
        uiCustomizationOptions.populateRules("option3", rule2);
        return uiCustomizationOptions;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization-options.json"));
    }

    @Override
    protected void validateJson(final JsonContent<UiCustomizationOptions> jsonContent) {
        jsonContent.assertThat().hasJsonPathBooleanValue("@.option2.user", false);
        jsonContent.assertThat().hasJsonPathBooleanValue("@.option3.user", true);
        jsonContent.assertThat().hasJsonPathBooleanValue("@.option3.bi", false);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<UiCustomizationOptions> objectContent) {
        final Map<String, UiCustomizationRule> rules = objectContent.getObject().any();
        assertThat(rules, hasKey("option1"));
        assertThat(rules, hasKey("option2"));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<UiCustomizationOptions> objectContent) {
        final Map<String, UiCustomizationRule> rules = objectContent.getObject().any();
        assertThat(rules, hasKey("option1"));
        assertThat(rules, hasKey("option2"));
        assertThat(rules, hasKey("option3"));
    }

    @Override
    protected void validateString(final String s) {

    }
}
