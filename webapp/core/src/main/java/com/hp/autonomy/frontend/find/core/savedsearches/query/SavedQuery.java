/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.ZonedDateTime;

@Entity
@DiscriminatorValue(SavedSearchType.Values.QUERY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedQuery.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedQuery extends SavedSearch<SavedQuery, SavedQuery.Builder> {
    @Column(name = "last_fetched_new_date")
    private ZonedDateTime dateNewDocsLastFetched;

    @Column(name = "last_fetched_date")
    private ZonedDateTime dateDocsLastFetched;

    private SavedQuery(final Builder builder) {
        super(builder);
        dateNewDocsLastFetched = builder.dateNewDocsLastFetched;
        dateDocsLastFetched = builder.dateDocsLastFetched;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    protected void mergeInternal(final SavedQuery other) {
        dateNewDocsLastFetched = other.dateNewDocsLastFetched == null ? dateNewDocsLastFetched : other.dateNewDocsLastFetched;
        dateDocsLastFetched = other.dateDocsLastFetched == null ? dateDocsLastFetched : other.dateDocsLastFetched;
    }

    @NoArgsConstructor
    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends SavedSearch.Builder<SavedQuery, Builder> {
        private ZonedDateTime dateNewDocsLastFetched;
        private ZonedDateTime dateDocsLastFetched;

        /**
         * Populate a builder with fields common to all {@link SavedSearch} types.
         */
        public Builder(final SavedSearch<?, ?> search) {
            super(search);
        }

        public Builder(final SavedQuery query) {
            super(query);
            dateNewDocsLastFetched = query.dateNewDocsLastFetched;
            dateDocsLastFetched = query.dateDocsLastFetched;
        }

        @Override
        public SavedQuery build() {
            return new SavedQuery(this);
        }
    }
}
