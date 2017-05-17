/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import javax.persistence.Transient;

@SuppressWarnings("WeakerAccess")
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonDeserialize(builder = FieldAndValue.FieldAndValueBuilder.class)
public class FieldAndValue {

    private String field;
    @Transient
    private String displayName;
    private String value;
    @Transient
    private String displayValue;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FieldAndValueBuilder {
    }
}
