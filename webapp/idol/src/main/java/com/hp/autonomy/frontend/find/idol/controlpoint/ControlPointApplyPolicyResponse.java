/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * ControlPoint apply-policy API response.
 */
@Getter
public class ControlPointApplyPolicyResponse {
    /**
     * Whether any changes happened.  False when all documents already had the policy applied.
     */
    private final boolean success;
    private final boolean partialApplication;

    ControlPointApplyPolicyResponse(
        @JsonProperty(value = "Success", required = true) final boolean success,
        @JsonProperty(value = "ItemsWillBeIgnored", required = true) final boolean partialApplication
    ) {
        this.success = success;
        this.partialApplication = partialApplication;
    }

}
