/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

/**
 * Where to store the Haven OnDemand persistent state (Haven OnDemand tokens, sessions etc.)
 */
public enum PersistentStateConfig {
    /** Store the token in memory - only good for a single Find node */
    INMEMORY,

    /** Store the token in Redis - use this for clustered Find */
    REDIS
}
