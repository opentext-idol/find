/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class MessageOfTheDayConfigValidator implements Validator<MessageOfTheDayConfig> {
    @Override
    public ValidationResult<Void> validate(final MessageOfTheDayConfig config) {
        return new ValidationResult<>(true);
    }

    @Override
    public Class<MessageOfTheDayConfig> getSupportedClass() {
        return MessageOfTheDayConfig.class;
    }
}
