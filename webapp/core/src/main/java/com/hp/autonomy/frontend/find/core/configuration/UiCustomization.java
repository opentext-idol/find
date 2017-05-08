/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Builder(toBuilder = true)
@Getter
@JsonDeserialize(builder = UiCustomization.UiCustomizationBuilder.class)
public class UiCustomization implements ConfigurationComponent<UiCustomization> {
    private final UiCustomizationOptions options;
    @Singular("parametricNeverShowItem")
    private final Collection<FieldPath> parametricNeverShow;
    @Singular("parametricAlwaysShowItem")
    private final Collection<FieldPath> parametricAlwaysShow;
    @Singular("parametricOrderItem")
    private final Collection<FieldPath> parametricOrder;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, String> specialUrlPrefixes;
    private final String errorCallSupportString;

    @Override
    public UiCustomization merge(final UiCustomization uiCustomization) {
        if(uiCustomization == null) {
            return this;
        } else {
            final Map<String, String> specialUrlPrefixes = new HashMap<>(uiCustomization.specialUrlPrefixes);
            specialUrlPrefixes.putAll(this.specialUrlPrefixes);
            return builder()
                    .options(Optional.ofNullable(options).map(o -> o.merge(uiCustomization.options)).orElse(uiCustomization.options))
                    .parametricNeverShow(CollectionUtils.isNotEmpty(parametricNeverShow) ? parametricNeverShow : uiCustomization.parametricNeverShow)
                    .parametricAlwaysShow(CollectionUtils.isNotEmpty(parametricAlwaysShow) ? parametricAlwaysShow : uiCustomization.parametricAlwaysShow)
                    .parametricOrder(CollectionUtils.isNotEmpty(parametricOrder) ? parametricOrder : uiCustomization.parametricOrder)
                    .specialUrlPrefixes(specialUrlPrefixes)
                    .errorCallSupportString(uiCustomization.errorCallSupportString)
                    .build();
        }
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
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
