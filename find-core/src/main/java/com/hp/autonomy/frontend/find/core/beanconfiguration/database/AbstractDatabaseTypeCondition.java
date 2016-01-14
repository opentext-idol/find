/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AbstractEnumCondition;

public abstract class AbstractDatabaseTypeCondition extends AbstractEnumCondition<DatabaseTypeConfig> {

    private static final DatabaseTypeConfig DEFAULT_VALUE = DatabaseTypeConfig.H2PERSISTENT;

    protected AbstractDatabaseTypeCondition(final DatabaseTypeConfig databaseTypeConfig) {
        super("hp.find.databaseType", databaseTypeConfig, DEFAULT_VALUE, DatabaseTypeConfig.class);
    }
}
