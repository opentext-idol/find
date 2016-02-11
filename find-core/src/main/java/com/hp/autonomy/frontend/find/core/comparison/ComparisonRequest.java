/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@JsonDeserialize(builder = ComparisonRequest.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComparisonRequest<S extends Serializable> implements Serializable {
    private static final long serialVersionUID = -604283886267152277L;

    private final QueryRestrictions<S> firstRestrictions;
    private final QueryRestrictions<S> secondRestrictions;
    private final String firstQueryStateToken;
    private final String secondQueryStateToken;

    private ComparisonRequest(final Builder<S> builder) {
        firstRestrictions = builder.firstRestrictions;
        secondRestrictions = builder.secondRestrictions;
        firstQueryStateToken = builder.firstQueryStateToken;
        secondQueryStateToken = builder.secondQueryStateToken;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @Setter
    @Accessors(chain = true)
    public static class Builder<S extends Serializable> {
        private QueryRestrictions<S> firstRestrictions;
        private QueryRestrictions<S> secondRestrictions;
        private String firstQueryStateToken;
        private String secondQueryStateToken;

        public ComparisonRequest<S> build() {
            return new ComparisonRequest<>(this);
        }
    }
}
