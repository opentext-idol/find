/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AbstractBackendCondition;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BackendConfig;

public class HodCondition extends AbstractBackendCondition {
    protected HodCondition() {
        super(BackendConfig.HAVEN_ON_DEMAND);
    }
}
