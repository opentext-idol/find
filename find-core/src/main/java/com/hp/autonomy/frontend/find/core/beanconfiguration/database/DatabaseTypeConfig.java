/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */


package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

/**
 * Which relational database implementation to use for storage
 */
public enum DatabaseTypeConfig {

    /** Store data persistently in H2 */
    H2PERSISTENT,

    /** Store data in memory in H2 */
    H2INMEMORY
}
