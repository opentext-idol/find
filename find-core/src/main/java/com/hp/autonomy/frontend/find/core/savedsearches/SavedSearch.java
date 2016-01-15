/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedSearch<I> {
    private final Long id;
    private final String title;
    private final String queryText;
    private final Set<I> indexes;
    private final Set<FieldAndValue> parametricValues;
    private final DateTime minDate;
    private final DateTime maxDate;
    private final DateTime dateCreated;
    private final DateTime dateModified;

    @JsonCreator
    public SavedSearch(
            @JsonProperty("id") final Long id,
            @JsonProperty("title") final String title,
            @JsonProperty("queryText") final String queryText,
            @JsonProperty("indexes") final Set<I> indexes,
            @JsonProperty("parametricValues") final Set<FieldAndValue> parametricValues,
            @JsonProperty("minDate") final DateTime minDate,
            @JsonProperty("maxDate") final DateTime maxDate,
            @JsonProperty("dateCreated") final DateTime dateCreated,
            @JsonProperty("dateModified") final DateTime dateModified
    ) {
        this.id = id;
        this.title = title;
        this.queryText = queryText;
        this.indexes = indexes;
        this.parametricValues = parametricValues;
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Builder<I> {
        private Long id;
        private String title;
        private String queryText;
        private Set<I> indexes;
        private Set<FieldAndValue> parametricValues;
        private DateTime minDate;
        private DateTime maxDate;
        private DateTime dateCreated;
        private DateTime dateModified;

        public Builder(final SavedSearch<I> search) {
            id = search.id;
            title = search.title;
            queryText = search.queryText;
            indexes = search.indexes;
            parametricValues = search.parametricValues;
            minDate = search.minDate;
            maxDate = search.maxDate;
            dateCreated = search.dateCreated;
            dateModified = search.dateModified;
        }

        public SavedSearch<I> build() {
            return new SavedSearch<>(
                    id,
                    title,
                    queryText,
                    indexes,
                    parametricValues,
                    minDate,
                    maxDate,
                    dateCreated,
                    dateModified
            );
        }
    }
}
