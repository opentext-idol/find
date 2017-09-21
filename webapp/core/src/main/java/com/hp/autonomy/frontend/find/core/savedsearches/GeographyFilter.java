/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA embeddable representation of a geography filter applied to a search.
 */
@Embeddable
@Data
@NoArgsConstructor
public class GeographyFilter {
    @Column(name = SavedSearch.GeographyFilterTable.Column.FIELD)
    private String field;

    @Column(name = SavedSearch.GeographyFilterTable.Column.JSON)
    private String json;

    @JsonCreator
    public GeographyFilter(
            @JsonProperty("field") final String field,
            @JsonProperty("json") final String json
    ) {
        this.field = field;
        this.json = json;
    }
}
