/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AbstractEnumCondition;

public abstract class AbstractPersistentStateCondition extends AbstractEnumCondition<PersistentStateConfig> {

    private static final PersistentStateConfig DEFAULT_VALUE = PersistentStateConfig.REDIS;

    protected AbstractPersistentStateCondition(final PersistentStateConfig persistentStateConfig) {
        super("hp.find.persistentState", persistentStateConfig, DEFAULT_VALUE, PersistentStateConfig.class);
    }

}
