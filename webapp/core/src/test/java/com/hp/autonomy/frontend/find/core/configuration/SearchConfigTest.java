package com.hp.autonomy.frontend.find.core.configuration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

import java.util.Collections;

public class SearchConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final SearchConfig config = SearchConfig.builder().build();
        config.basicValidate("search");
    }

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final SearchConfig config = SearchConfig.builder()
            .defaultSortOption("option 2")
            .sortOptions(new ImmutableMap.Builder<String, SearchSortOption>()
                .put("option 1", new SearchSortOption("relevance", "label 1"))
                .put("option 2", new SearchSortOption("date", null)).build())
            .build();
        config.basicValidate("search");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_defaultSortOptionMissing() throws ConfigException {
        final SearchConfig config = SearchConfig.builder()
            .defaultSortOption("option 2")
            .sortOptions(Collections.singletonMap(
                "option 1", new SearchSortOption("relevance", null)))
            .build();
        config.basicValidate("search");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_sortOptionInvalid() throws ConfigException {
        final SearchConfig config = SearchConfig.builder()
            .defaultSortOption("option 2")
            .sortOptions(new ImmutableMap.Builder<String, SearchSortOption>()
                .put("option 1", new SearchSortOption(null, "label 1"))
                .put("option 2", new SearchSortOption("date", null)).build())
            .build();
        config.basicValidate("search");
    }

}
