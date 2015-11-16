/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

public abstract class AbstractBackendCondition extends AbstractEnumCondition<BackendConfig> {
    private static final BackendConfig DEFAULT_VALUE = BackendConfig.HAVEN_ON_DEMAND;

    protected AbstractBackendCondition(final BackendConfig backendConfig) {
        super("hp.find.backend", backendConfig, DEFAULT_VALUE, BackendConfig.class);
    }
}
