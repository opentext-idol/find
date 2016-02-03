/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(SavedSearchType.Values.QUERY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedQuery.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedQuery extends SavedSearch {

    private SavedQuery(final Builder builder) {
        super(builder);
    }

    @NoArgsConstructor
    @Getter
    @Accessors(chain = true)
    public static class Builder extends SavedSearch.Builder<SavedQuery> {

        public Builder(final SavedQuery query) {
            super(query);
        }

        @Override
        public SavedQuery build() {
            return new SavedQuery(this);
        }
    }
}

