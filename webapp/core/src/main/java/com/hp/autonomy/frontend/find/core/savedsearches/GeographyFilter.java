/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
