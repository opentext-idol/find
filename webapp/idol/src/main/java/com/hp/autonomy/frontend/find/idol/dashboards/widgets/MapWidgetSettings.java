package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.find.core.configuration.InitialLocation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "InstanceVariableOfConcreteClass"})
@Data
@Builder
@JsonDeserialize(builder = MapWidgetSettings.MapWidgetSettingsBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class MapWidgetSettings extends SimpleComponent<MapWidgetSettings> implements WidgetSettings<MapWidgetSettings> {
    private final Map<String, Object> widgetSettings;
    private final Integer maxResults;
    private final Collection<String> locationFieldPairs;
    private final InitialLocation centerCoordinates;
    private final Integer zoomLevel;
    private final Boolean clusterMarkers;
    private final Boolean disableAutoZoom;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class MapWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private Integer maxResults;
        private Collection<String> locationFieldPairs;
        private InitialLocation centerCoordinates;
        private Integer zoomLevel;
        private Boolean clusterMarkers;
        private Boolean disableAutoZoom;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public MapWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
