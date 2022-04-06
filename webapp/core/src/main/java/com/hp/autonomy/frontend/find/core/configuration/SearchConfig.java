package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Search-related configuration.
 */
@Builder
@Getter
@JsonDeserialize(builder = SearchConfig.SearchConfigBuilder.class)
public class SearchConfig {
    /**
     * The default option for sorting search results.  One of the keys in {@link sortOptions}.
     */
    private final String defaultSortOption;
    /**
     * The available options for sorting search results.
     */
    private final Map<String, SearchSortOption> sortOptions;

    public void basicValidate(final String configSection) throws ConfigException {
        if (!sortOptions.containsKey(defaultSortOption)) {
            throw new ConfigException(configSection,
                "defaultSortOption must be a key in sortOptions");
        }
        for (final Map.Entry<String, SearchSortOption> sortOption : sortOptions.entrySet()) {
            sortOption.getValue().basicValidate(
                configSection + ".sortOptions." + sortOption.getKey());
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class SearchConfigBuilder {
        private String defaultSortOption = "relevance";
        private Map<String, SearchSortOption> sortOptions =
            new ImmutableMap.Builder<String, SearchSortOption>()
                .put("relevance", new SearchSortOption("relevance", null))
                .put("date", new SearchSortOption("date", null))
                .put("reverseDate", new SearchSortOption("reversedate", null)).build();
    }

}
