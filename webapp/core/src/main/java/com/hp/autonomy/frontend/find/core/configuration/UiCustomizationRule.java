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

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "DefaultAnnotationParam"})
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = UiCustomizationRule.UiCustomizationRuleBuilder.class)
public class UiCustomizationRule extends SimpleComponent<UiCustomizationRule> implements ConfigurationComponent<UiCustomizationRule> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, Object> roleMap;

    @JsonAnyGetter
    public Map<String, Object> getRoleMap() {
        return Collections.unmodifiableMap(roleMap);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class UiCustomizationRuleBuilder {
        private Map<String, Object> roleMap = new HashMap<>();

        @JsonAnySetter
        public UiCustomizationRuleBuilder populateRule(final String key, final Object value) {
            roleMap.put(key, value);
            return this;
        }
    }
}
