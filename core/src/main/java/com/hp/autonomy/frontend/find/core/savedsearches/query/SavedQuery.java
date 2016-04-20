/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(SavedSearchType.Values.QUERY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedQuery.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDefs(@TypeDef(name = SavedSearch.JADIRA_TYPE_NAME, typeClass = PersistentDateTime.class))
public class SavedQuery extends SavedSearch<SavedQuery> {
    @Column(name = "last_fetched_new_date")
    @Type(type = JADIRA_TYPE_NAME)
    private DateTime dateNewDocsLastFetched;
    
    private SavedQuery(final Builder builder) {
        super(builder);
        dateNewDocsLastFetched = builder.dateNewDocsLastFetched;
    }

    @Override
    protected void mergeInternal(final SavedQuery other) {
        dateNewDocsLastFetched = other.getDateNewDocsLastFetched() == null ? dateNewDocsLastFetched : other.getDateNewDocsLastFetched();
    }

    @NoArgsConstructor
    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends SavedSearch.Builder<SavedQuery> {
        private DateTime dateNewDocsLastFetched;

        public Builder(final SavedQuery query) {
            super(query);
            dateNewDocsLastFetched = query.dateNewDocsLastFetched;
        }

        @Override
        public SavedQuery build() {
            return new SavedQuery(this);
        }
    }
}

