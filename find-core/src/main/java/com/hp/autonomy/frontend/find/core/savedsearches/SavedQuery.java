/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedSearch.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedQuery<I> extends SavedSearch<I> {

    private SavedQuery(final Builder<I> builder) {
        super(builder);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Builder<I> extends SavedSearch.Builder<I, SavedQuery<I>> {

        public Builder(final SavedQuery<I> query) {
            super(query);
        }

        @Override
        public SavedQuery<I> build() {
            return new SavedQuery<>(this);
        }
    }
}
