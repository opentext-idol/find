/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class SavedSearch<I> {
    private final Integer id;
    private final String title;
    private final String queryText;
    private final Set<I> indexes;
    private final Set<FieldAndValue> parametricValues;
    private final DateTime minDate;
    private final DateTime maxDate;
    private final DateTime dateCreated;
    private final DateTime dateModified;

    protected SavedSearch(Builder<I, ?> builder) {
        id = builder.id;
        title = builder.title;
        queryText = builder.queryText;
        indexes = builder.indexes;
        parametricValues = builder.parametricValues;
        minDate = builder.minDate;
        maxDate = builder.maxDate;
        dateCreated = builder.dateCreated;
        dateModified = builder.dateModified;
    }

    @Getter
    @NoArgsConstructor
    public static abstract class Builder<I, T extends SavedSearch<I>> {
        private Integer id;
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

        public abstract T build();

        public Builder<I, T> setId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder<I, T> setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder<I, T> setQueryText(String queryText) {
            this.queryText = queryText;
            return this;
        }

        public Builder<I, T> setIndexes(Set<I> indexes) {
            this.indexes = indexes;
            return this;
        }

        public Builder<I, T> setParametricValues(Set<FieldAndValue> parametricValues) {
            this.parametricValues = parametricValues;
            return this;
        }

        public Builder<I, T> setMinDate(DateTime minDate) {
            this.minDate = minDate;
            return this;
        }

        public Builder<I, T> setMaxDate(DateTime maxDate) {
            this.maxDate = maxDate;
            return this;
        }

        public Builder<I, T> setDateCreated(DateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder<I, T> setDateModified(DateTime dateModified) {
            this.dateModified = dateModified;
            return this;
        }

    }
}
