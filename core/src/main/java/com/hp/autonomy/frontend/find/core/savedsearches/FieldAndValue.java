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

@Embeddable
@Data
@NoArgsConstructor
public class FieldAndValue {

    private String field;
    private String value;

    @JsonCreator
    public FieldAndValue(
            @JsonProperty("field") final String field,
            @JsonProperty("value") final String value
    ) {
        this.field = field;
        this.value = value;
    }
}
