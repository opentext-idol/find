/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@SuppressWarnings("WeakerAccess")
@Embeddable
@Data
@NoArgsConstructor
public class ParametricRange {
    private String field;
    private long min;
    private long max;
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    // Don't ever reorder these as we are using ordinal enumeration
    public enum Type {
        Date,
        Numeric
    }

    @JsonCreator
    public ParametricRange(
            @JsonProperty("field") final String field,
            @JsonProperty("min") final long min,
            @JsonProperty("max") final long max,
            @JsonProperty("type") final Type type
    ) {
        this.field = field;
        this.min = min;
        this.max = max;
        this.type = type;
    }
}
