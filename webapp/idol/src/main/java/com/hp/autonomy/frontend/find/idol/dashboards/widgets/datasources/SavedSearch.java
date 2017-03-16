/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(builder = SavedSearch.SavedSearchBuilder.class)
public class SavedSearch extends WidgetDatasource<SavedSearch, SavedSearchConfig> implements SavedSearchDatasource {
    @Builder
    public SavedSearch(final String source, final SavedSearchConfig config) {
        super(source, config);
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class SavedSearchBuilder {
    }
}
