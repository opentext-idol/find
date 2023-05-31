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
