/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

@SuppressWarnings("WeakerAccess")
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonDeserialize(builder = ParametricRange.ParametricRangeBuilder.class)
public class ParametricRange {
    private String field;
    @Transient
    private String displayName;
    private double min;
    private double max;
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    // Don't ever reorder these as we are using ordinal enumeration
    public enum Type {
        Date,
        Numeric
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ParametricRangeBuilder {
    }
}
