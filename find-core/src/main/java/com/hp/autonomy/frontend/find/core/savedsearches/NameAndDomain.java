/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// TODO: Either replace this with ResourceIdentifier or use database entity.
@Data
public class NameAndDomain {
    private final String domain;
    private final String name;

    public NameAndDomain(
            @JsonProperty("domain") final String domain,
            @JsonProperty("name") final String name
    ) {
        this.domain = domain;
        this.name = name;
    }
}
