/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class AbstractPersistentStateCondition implements Condition {

    private static final String DEFAULT_VALUE = "REDIS";

    private final PersistentStateConfig persistentStateConfig;

    protected AbstractPersistentStateCondition(final PersistentStateConfig persistentStateConfig) {
        this.persistentStateConfig = persistentStateConfig;
    }

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return getProperty(context) == persistentStateConfig;
    }

    private PersistentStateConfig getProperty(final ConditionContext context) {
        return PersistentStateConfig.valueOf(context.getEnvironment().getProperty("hp.find.persistentState", DEFAULT_VALUE));
    }

}
