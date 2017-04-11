package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Data
@Builder
@JsonDeserialize(builder = TopicMapWidgetSettings.TopicMapWidgetSettingsBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class TopicMapWidgetSettings extends SimpleComponent<TopicMapWidgetSettings> implements WidgetSettings<TopicMapWidgetSettings> {
    private final Map<String, Object> widgetSettings;
    private final Integer maxResults;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class TopicMapWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private Integer maxResults;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public TopicMapWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
