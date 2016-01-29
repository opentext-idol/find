/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * Embeddable version of what is essentially a ResourceIdentifier.  Defines a unified entity to be used
 * as part of a {@link SavedSearch}, so we don't have to parametrise its type (which causes havoc for JPA/hibernate).
 */
@Embeddable
@Data
@NoArgsConstructor
public class EmbeddableIndex {
    private String name;
    private String domain;

    @JsonCreator
    public EmbeddableIndex(
            @JsonProperty("name") final String name,
            @JsonProperty("domain") final String domain
    ) {
        this.name = name;
        this.domain = domain;
    }
}
