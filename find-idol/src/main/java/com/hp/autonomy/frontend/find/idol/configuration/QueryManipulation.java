/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import com.hp.autonomy.types.requests.qms.actions.typeahead.params.ModeParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;

@Data
@JsonDeserialize(builder = QueryManipulation.Builder.class)
public class QueryManipulation implements ConfigurationComponent {
    private final ServerConfig server;
    private final Boolean expandQuery;
    private final String blacklist;
    private final Boolean enabled;
    private final ModeParam typeAheadMode;

    private QueryManipulation(final Builder builder) {
        server = builder.server;
        expandQuery = builder.expandQuery;
        blacklist = builder.blacklist;
        enabled = builder.enabled;
        typeAheadMode = builder.typeAheadMode;
    }

    public QueryManipulation merge(final QueryManipulation queryManipulation) {
        if (queryManipulation == null) {
            return this;
        } else {
            return new Builder()
                    .setServer(server == null ? queryManipulation.server : server.merge(queryManipulation.server))
                    .setExpandQuery(expandQuery == null ? queryManipulation.expandQuery : expandQuery)
                    .setBlacklist(blacklist == null ? queryManipulation.blacklist : blacklist)
                    .setEnabled(enabled == null ? queryManipulation.enabled : enabled)
                    .setTypeAheadMode(typeAheadMode == null ? queryManipulation.typeAheadMode : typeAheadMode)
                    .build();
        }
    }

    @Override
    public boolean isEnabled() {
        return BooleanUtils.isTrue(enabled);
    }

    public void basicValidate() throws ConfigException {
        if (isEnabled()) {
            if (server == null) {
                throw new ConfigException("QMS", "QMS is enabled but no corresponding server details have been provided");
            }

            server.basicValidate("QMS");
        }
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ServerConfig server;
        private Boolean expandQuery;
        private String blacklist;
        private Boolean enabled;
        private ModeParam typeAheadMode;

        public QueryManipulation build() {
            return new QueryManipulation(this);
        }
    }
}
