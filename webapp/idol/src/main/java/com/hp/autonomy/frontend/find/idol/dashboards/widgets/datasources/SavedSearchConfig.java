package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Builder
@Data
@JsonDeserialize(builder = SavedSearchConfig.SavedSearchConfigBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class SavedSearchConfig extends SimpleComponent<SavedSearchConfig> implements WidgetDatasourceConfig<SavedSearchConfig> {
    private final Long id;
    private final SavedSearchType type;
    private final Map<String, Object> config;

    @Override
    @JsonAnyGetter
    public Map<String, Object> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    @SuppressWarnings({"WeakerAccess", "FieldMayBeFinal"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class SavedSearchConfigBuilder {
        private Map<String, Object> config = new HashMap<>();

        @SuppressWarnings("unused")
        @JsonAnySetter
        public SavedSearchConfigBuilder configEntry(final String key, final Object value) {
            config.put(key, value);
            return this;
        }
    }
}
