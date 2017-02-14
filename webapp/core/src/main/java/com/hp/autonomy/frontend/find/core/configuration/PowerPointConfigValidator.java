/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class PowerPointConfigValidator implements Validator<PowerPointConfig> {
    @Override
    public ValidationResult<Void> validate(final PowerPointConfig config) {
        boolean valid = true;

        try {
            config.basicValidate("powerPoint");
        }
        catch(ConfigException e) {
            valid = false;
        }

        return new ValidationResult<>(valid);
    }

    @Override
    public Class<PowerPointConfig> getSupportedClass() {
        return PowerPointConfig.class;
    }
}
