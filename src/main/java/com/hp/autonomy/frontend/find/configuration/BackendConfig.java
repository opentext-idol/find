/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

/**
 * What kind of backend to use
 */
public enum BackendConfig {

    /**
     * Use Haven OnDemand as the backend for Find.
     */
    HAVEN_ON_DEMAND,

    /**
     * Use your own IDOL servers (sold separately) as the backend for Find.
     */
    IDOL

}
