/*
 * Copyright 2020 Open Text.
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
