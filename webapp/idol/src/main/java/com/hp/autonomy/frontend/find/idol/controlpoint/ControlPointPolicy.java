/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A policy that can be applied to documents.
 */
@Getter
@EqualsAndHashCode
public class ControlPointPolicy {
    private final String id;
    private final String name;
    private final boolean isPublished;
    private final boolean isActive;

    public ControlPointPolicy(
        @JsonProperty(value = "Id", required = true) final String id,
        @JsonProperty(value = "Name", required = true) final String name,
        @JsonProperty("IsPublished") final boolean isPublished,
        @JsonProperty("IsActive") final boolean isActive
    ) {
        this.id = id;
        this.name = name;
        this.isPublished = isPublished;
        this.isActive = isActive;
    }

}
