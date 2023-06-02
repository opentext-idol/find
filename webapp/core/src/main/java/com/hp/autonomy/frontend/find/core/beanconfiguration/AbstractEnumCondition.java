/*
 * Copyright 2015-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class AbstractEnumCondition<T extends Enum<T>> implements Condition {

    private final String[] systemProperties;
    private final T specifiedValue;
    private final T defaultValue;
    private final Class<T> typeToken;

    protected AbstractEnumCondition(final String[] systemProperties, final T specifiedValue, final T defaultValue, final Class<T> typeToken) {
        this.systemProperties = systemProperties;
        this.specifiedValue = specifiedValue;
        this.defaultValue = defaultValue;
        this.typeToken = typeToken;
    }

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return getProperty(context) == specifiedValue;
    }

    private T getProperty(final ConditionContext context) {
        for(String key : systemProperties) {
            final String prop = context.getEnvironment().getProperty(key);
            if (StringUtils.isNotBlank(prop)) {
                return Enum.valueOf(typeToken, prop);
            }
        }

        return Enum.valueOf(typeToken, defaultValue.name());
    }
}
