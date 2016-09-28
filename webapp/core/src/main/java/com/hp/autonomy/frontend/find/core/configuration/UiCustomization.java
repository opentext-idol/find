/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Getter;
import lombok.experimental.Builder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Builder(fluent = false)
@Getter
@JsonDeserialize(builder = UiCustomization.UiCustomizationBuilder.class)
public class UiCustomization implements ConfigurationComponent<UiCustomization> {
    private final UiCustomizationOptions options;
    private final Collection<String> parametricBlacklist;
    private final Collection<String> parametricWhitelist;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, String> specialUrlPrefixes;
    private final String errorCallSupportString;

    @Override
    public UiCustomization merge(final UiCustomization uiCustomization) {
        if (uiCustomization == null) {
            return this;
        } else {
            final Map<String, String> specialUrlPrefixes = new HashMap<>(uiCustomization.specialUrlPrefixes);
            specialUrlPrefixes.putAll(this.specialUrlPrefixes);
            return builder()
                    .setOptions(Optional.ofNullable(options).map(o -> o.merge(uiCustomization.options)).orElse(uiCustomization.options))
                    .setParametricBlacklist(parametricBlacklist != null ? parametricBlacklist : uiCustomization.parametricBlacklist)
                    .setParametricWhitelist(parametricWhitelist != null ? parametricWhitelist : uiCustomization.parametricWhitelist)
                    .setSpecialUrlPrefixes(specialUrlPrefixes)
                    .setErrorCallSupportString(uiCustomization.errorCallSupportString)
                    .build();
        }
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "set")
    public static class UiCustomizationBuilder {
        @SuppressWarnings("FieldMayBeFinal")
        private Map<String, String> specialUrlPrefixes = new HashMap<>();

        @SuppressWarnings("unused")
        @JsonAnySetter
        public void populateSpecialUrlPrefixes(final String contentType, final String prefix) {
            specialUrlPrefixes.put(contentType, prefix);
        }

        @JsonAnyGetter
        public Map<String, String> any() {
            return Collections.unmodifiableMap(specialUrlPrefixes);
        }
    }
}
