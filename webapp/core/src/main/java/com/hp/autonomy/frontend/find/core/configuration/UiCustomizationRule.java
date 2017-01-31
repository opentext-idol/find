/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
