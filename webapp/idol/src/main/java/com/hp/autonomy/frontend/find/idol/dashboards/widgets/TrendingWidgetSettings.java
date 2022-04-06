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

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = TrendingWidgetSettings.TrendingWidgetSettingsBuilder.class)
public class TrendingWidgetSettings extends SimpleComponent<TrendingWidgetSettings> implements WidgetSettings<TrendingWidgetSettings> {
    private static final String SECTION = "Trending Dashboard";
    private static final Set<String> VALID_COLOURS = ImmutableSet.of("blue", "light-blue", "orange", "pink", "light-pink", "green", "light-green", "red", "purple", "yellow");
    private final Map<String, Object> widgetSettings;
    private final TagName parametricField;
    private final TagName dateField;
    private final Integer maxValues;
    private final Integer numberOfBuckets;
    private final ZonedDateTime minDate;
    private final ZonedDateTime maxDate;
    private final List<TrendingValue> values;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (parametricField == null) {
            throw new ConfigException(SECTION, "Trending widget config must specify a parametric field");
        }

        if (values != null) {
            final List<TrendingValue> invalidValues = values.stream()
                    .filter(v -> !VALID_COLOURS.contains(v.color))
                    .collect(Collectors.toList());
            if (!invalidValues.isEmpty()) {
                throw new ConfigException(SECTION, "The following parametric values have invalid colours associated: " + invalidValues);
            }
        }

        if (minDate != null && maxDate != null) {
            if (minDate.isAfter(maxDate)) {
                throw new ConfigException(SECTION, "Invalid date range. Configured min date is greater than max date.");
            }
        }
        super.basicValidate(section);
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingValue {
        @JsonProperty("name")
        private String name;
        @JsonProperty("color")
        private String color;
    }

    @SuppressWarnings({"FieldMayBeFinal", "WeakerAccess", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class TrendingWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private TagName parametricField;
        private TagName dateField;
        private Integer maxValues;
        private Integer numberOfBuckets;
        private ZonedDateTime minDate;
        private ZonedDateTime maxDate;
        private List<TrendingValue> values;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public TrendingWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
