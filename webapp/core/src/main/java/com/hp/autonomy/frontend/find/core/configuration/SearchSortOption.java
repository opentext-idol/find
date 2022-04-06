package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * An option for sorting search results.
 */
@Builder
@Getter
@JsonDeserialize(builder = SearchSortOption.SearchSortOptionBuilder.class)
public class SearchSortOption {
    /**
     * The value sent to IDOL in the `sort` parameter.
     */
    private final String sort;
    /**
     * Label displayed to the user.
     */
    private final String label;

    public void basicValidate(final String configSection) throws ConfigException {
        if (StringUtils.isBlank(sort)) {
            throw new ConfigException(configSection, "sort must be provided");
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class SearchSortOptionBuilder { }

}
