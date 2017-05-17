package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Builder
@JsonDeserialize(builder = SimpleWidgetSettings.SimpleWidgetSettingsBuilder.class)
@EqualsAndHashCode(callSuper = false)
@ToString
public class SimpleWidgetSettings extends SimpleComponent<SimpleWidgetSettings> implements WidgetSettings<SimpleWidgetSettings> {
    private final Map<String, Object> widgetSettings;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class SimpleWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();

        @SuppressWarnings("unused")
        @JsonAnySetter
        public SimpleWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
