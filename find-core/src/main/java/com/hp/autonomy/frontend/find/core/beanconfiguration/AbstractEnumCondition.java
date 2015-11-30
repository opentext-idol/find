/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class AbstractEnumCondition<T extends Enum<T>> implements Condition {

    private final String systemProperty;
    private final T specifiedValue;
    private final T defaultValue;
    private final Class<T> typeToken;

    protected AbstractEnumCondition(final String systemProperty, final T specifiedValue, final T defaultValue, final Class<T> typeToken) {
        this.systemProperty = systemProperty;
        this.specifiedValue = specifiedValue;
        this.defaultValue = defaultValue;
        this.typeToken = typeToken;
    }

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return getProperty(context) == specifiedValue;
    }

    private T getProperty(final ConditionContext context) {
        return Enum.valueOf(typeToken, context.getEnvironment().getProperty(systemProperty, defaultValue.name()));
    }
}
