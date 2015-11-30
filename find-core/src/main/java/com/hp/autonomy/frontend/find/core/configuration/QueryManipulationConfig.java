/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class QueryManipulationConfig {
    public static final String SECTION = "queryManipulation";

    private final String profile;
    private final String index;

    QueryManipulationConfig(
            @JsonProperty("profile") final String profile,
            @JsonProperty("index") final String index
    ) {
        this.profile = profile;
        this.index = index;
    }

    public QueryManipulationConfig merge(final QueryManipulationConfig other) {
        if (other == null) {
            return this;
        }

        return new QueryManipulationConfig(
                profile == null ? other.profile : profile,
                index == null ? other.index : index
        );
    }

    public void basicValidate() throws ConfigException {
        if (StringUtils.isBlank(profile)) {
            throw new ConfigException(SECTION, "Query profile is required");
        }

        if (StringUtils.isBlank(index)) {
            throw new ConfigException(SECTION, "Query manipulation index is required");
        }
    }
}
