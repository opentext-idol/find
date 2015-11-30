/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@JsonDeserialize(builder = QueryManipulation.Builder.class)
public class QueryManipulation {
    private final Boolean expandQuery;
    private final String blacklist;

    private QueryManipulation(final Builder builder) {
        expandQuery = builder.expandQuery;
        blacklist = builder.blacklist;
    }

    public QueryManipulation merge(final QueryManipulation queryManipulation) {
        final Builder builder = new Builder();
        builder.setExpandQuery(expandQuery == null ? queryManipulation.expandQuery : expandQuery);
        builder.setBlacklist(blacklist == null ? queryManipulation.blacklist : blacklist);

        return builder.build();
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private Boolean expandQuery;
        private String blacklist;

        public Builder(final QueryManipulation queryManipulation) {
            expandQuery = queryManipulation.expandQuery;
            blacklist = queryManipulation.blacklist;
        }

        public QueryManipulation build() {
            return new QueryManipulation(this);
        }
    }
}
