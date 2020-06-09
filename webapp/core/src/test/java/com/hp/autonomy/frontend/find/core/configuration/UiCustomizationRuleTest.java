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
import org.hamcrest.Matchers;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

public class UiCustomizationRuleTest extends ConfigurationComponentTest<UiCustomizationRule> {
    @Override
    protected Class<UiCustomizationRule> getType() {
        return UiCustomizationRule.class;
    }

    @Override
    protected UiCustomizationRule constructComponent() {
        return UiCustomizationRule.builder()
            .populateRule("user", false)
            .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationRuleTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization-rule.json"));
    }

    @Override
    protected void validateJson(final JsonContent<UiCustomizationRule> jsonContent) {
        jsonContent.assertThat().hasJsonPathBooleanValue("@.user", false);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<UiCustomizationRule> objectContent) {
        final Map<String, Object> rules = objectContent.getObject().getRoleMap();
        assertThat(rules, hasKey("user"));
        assertThat(rules, hasKey("bi"));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<UiCustomizationRule> objectContent) {
        final Map<String, Object> rules = objectContent.getObject().getRoleMap();
        assertThat(rules, Matchers.hasEntry("user", false));
        assertThat(rules, Matchers.hasEntry("bi", true));
    }

    @Override
    protected void validateString(final String s) {
        assertTrue(s.contains("user"));
    }
}
