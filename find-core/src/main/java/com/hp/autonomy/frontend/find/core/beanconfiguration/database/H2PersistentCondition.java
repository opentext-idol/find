/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration.database;

public class H2PersistentCondition extends AbstractDatabaseTypeCondition {

    protected H2PersistentCondition() {
        super(DatabaseTypeConfig.H2PERSISTENT);
    }

}
