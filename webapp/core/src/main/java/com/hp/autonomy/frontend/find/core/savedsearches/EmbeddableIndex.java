/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable version of what is essentially a ResourceIdentifier.  Defines a unified entity to be used
 * as part of a {@link SavedSearch}, so we don't have to parametrise its type (which causes havoc for JPA/hibernate).
 */
@Embeddable
@Data
@NoArgsConstructor
// Exclude domain when null to avoid confusing client
// TODO: come up with better solution
@JsonInclude(JsonInclude.Include.NON_NULL)
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
