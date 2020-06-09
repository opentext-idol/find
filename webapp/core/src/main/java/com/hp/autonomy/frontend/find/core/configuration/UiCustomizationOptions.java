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
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class UiCustomizationOptions implements ConfigurationComponent<UiCustomizationOptions> {
    private final Map<String, UiCustomizationRule> rules = new HashMap<>();

    @JsonAnySetter
    public void populateRules(final String key, final UiCustomizationRule rule) {
        rules.put(key, rule);
    }

    @JsonAnyGetter
    public Map<String, UiCustomizationRule> any() {
        return Collections.unmodifiableMap(rules);
    }

    @Override
    public UiCustomizationOptions merge(final UiCustomizationOptions options) {
        if(options != null) {
            for(final Map.Entry<String, UiCustomizationRule> entry : options.rules.entrySet()) {
                if(rules.containsKey(entry.getKey())) {
                    rules.get(entry.getKey()).merge(entry.getValue());
                } else {
                    rules.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {}
}
