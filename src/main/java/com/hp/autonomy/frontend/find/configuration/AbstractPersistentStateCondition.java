/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

public abstract class AbstractPersistentStateCondition implements Condition {

    private static final String DEFAULT_VALUE = "INMEMORY";

    protected PersistentStateConfig getProperty(final ConditionContext context) {
        return PersistentStateConfig.valueOf(context.getEnvironment().getProperty("hp.find.persistentState", DEFAULT_VALUE));
    }

}
