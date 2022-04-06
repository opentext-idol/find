/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import java.util.ArrayList;
import java.util.List;

/**
 * List of policies.
 */
public class ControlPointPolicies extends ArrayList<ControlPointPolicy> {

    public ControlPointPolicies() {
        super();
    }

    public ControlPointPolicies(final List<ControlPointPolicy> policies) {
        super(policies);
    }

}
