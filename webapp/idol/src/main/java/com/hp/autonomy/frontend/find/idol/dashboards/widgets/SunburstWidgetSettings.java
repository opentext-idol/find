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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = SunburstWidgetSettings.SunburstWidgetSettingsBuilder.class)
public class SunburstWidgetSettings extends SimpleComponent<SunburstWidgetSettings> implements WidgetSettings<SunburstWidgetSettings> {
    private static final String SECTION = "Sunburst Dashboard";
    private final Map<String, Object> widgetSettings;
    private final TagName firstField;
    private final TagName secondField;
    private final Integer maxLegendEntries;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (firstField == null) {
            throw new ConfigException(SECTION, "Sunburst dashboard config must specify a first field");
        }

        super.basicValidate(section);
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"FieldMayBeFinal", "WeakerAccess", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class SunburstWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private TagName firstField;
        private TagName secondField;
        private Integer maxLegendEntries;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public SunburstWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
