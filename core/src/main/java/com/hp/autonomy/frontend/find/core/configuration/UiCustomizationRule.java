/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.hp.autonomy.frontend.configuration.ConfigException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class UiCustomizationRule implements ConfigurationComponent<UiCustomizationRule> {
    private final Map<String, Object> roleMap = new HashMap<>();

    @JsonAnySetter
    public void populateRule(final String key, final Object value) {
        roleMap.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> any() {
        return Collections.unmodifiableMap(roleMap);
    }

    @Override
    public UiCustomizationRule merge(final UiCustomizationRule other) {
        if (other != null) {
            other.roleMap.entrySet().stream().filter(entry -> !roleMap.containsKey(entry.getKey())).forEach(entry -> roleMap.put(entry.getKey(), entry.getValue()));
        }

        return this;
    }

    @Override
    public void basicValidate(final String... options) throws ConfigException {
    }
}
