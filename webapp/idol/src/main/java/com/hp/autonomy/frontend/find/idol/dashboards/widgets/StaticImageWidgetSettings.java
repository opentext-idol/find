package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
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
@JsonDeserialize(builder = StaticImageWidgetSettings.StaticImageWidgetSettingsBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class StaticImageWidgetSettings extends SimpleComponent<StaticImageWidgetSettings> implements WidgetSettings<StaticImageWidgetSettings> {
    private final Map<String, Object> widgetSettings;
    private final String url;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getWidgetSettings() {
        return Collections.unmodifiableMap(widgetSettings);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (url == null) {
            throw new ConfigException("Static Image Widget", "Static Image Widget must contain a url");
        }

        super.basicValidate(section);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class StaticImageWidgetSettingsBuilder {
        private Map<String, Object> widgetSettings = new HashMap<>();
        private String url;

        @SuppressWarnings("unused")
        @JsonAnySetter
        public StaticImageWidgetSettingsBuilder widgetSetting(final String key, final Object value) {
            widgetSettings.put(key, value);
            return this;
        }
    }
}
