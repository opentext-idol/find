/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

public class RedisCondition extends AbstractPersistentStateCondition {
    public RedisCondition() {
        super(PersistentStateConfig.REDIS);
    }
}
