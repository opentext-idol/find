/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    protected void validateString(final String s) {}
}
