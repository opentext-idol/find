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
@JsonDeserialize(builder = VideoWidgetSettings.VideoWidgetSettingsBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class VideoWidgetSettings extends SimpleComponent<VideoWidgetSettings> implements WidgetSettings<VideoWidgetSettings> {
    private final Map<String, Object> widgetSettings;
    private final Boolean loop;
    private final Boolean audio;
    private final Integer searchResultNumber;
    private final Boolean restrictSearch;
    private final String crossOrigin;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class VideoWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private Boolean loop;
        private Boolean audio;
        private Integer searchResultNumber;
        private Boolean restrictSearch;
        private String crossOrigin;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public VideoWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
