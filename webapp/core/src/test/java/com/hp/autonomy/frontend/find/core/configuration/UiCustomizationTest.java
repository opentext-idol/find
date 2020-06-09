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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import org.apache.commons.io.IOUtils;
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY)
public class UiCustomizationTest extends ConfigurationComponentTest<UiCustomization> {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TagNameFactory tagNameFactory;

    @Override
    public void setUp() {
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

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
            .parametricOrderItem(tagNameFactory.getFieldPath("FIELD_Y"))
            .parametricOrderItem(tagNameFactory.getFieldPath("FIELD_X"))
            .specialUrlPrefixes(ImmutableMap.of("application/vnd.visio", "ms-visio:ofv|u|"))
            .errorCallSupportString("Other technical support message")
            .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/ui-customization.json"));
    }

    @Override
    protected void validateJson(final JsonContent<UiCustomization> jsonContent) {
        jsonContent.assertThat().hasJsonPathBooleanValue("@.options.option3.user", false);
        jsonContent.assertThat().hasJsonPathStringValue("@.parametricOrder[0]", "FIELD_Y");
        jsonContent.assertThat().hasJsonPathStringValue("@.parametricOrder[1]", "FIELD_X");
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
        assertThat(objectContent.getObject().getParametricNeverShow(), hasItem(tagNameFactory.getFieldPath("A_CLEAN_NUMERIC_FIELD")));
        assertThat(objectContent.getObject().getParametricAlwaysShow(), hasItem(tagNameFactory.getFieldPath("AUTN_DATE")));
        assertThat(objectContent.getObject().getSpecialUrlPrefixes(), hasKey("application/msword"));
        assertThat(objectContent.getObject().getSpecialUrlPrefixes(), hasKey("application/vnd.visio"));
        assertEquals("Other technical support message", objectContent.getObject().getErrorCallSupportString());
    }

    @Override
    protected void validateString(final String s) {}
}
