/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import org.springframework.stereotype.Component;

@Component
public class SavedSearchConfigValidator implements Validator<SavedSearchConfig> {
    @Override
    public ValidationResult<Void> validate(final SavedSearchConfig config) {
        boolean valid = true;
        if (config.getPollForUpdates() != null && config.getPollForUpdates() && (config.getPollingInterval() == null || config.getPollingInterval() < 0)) {
            valid = false;
        }

        return new ValidationResult<>(valid);
    }

    @Override
    public Class<SavedSearchConfig> getSupportedClass() {
        return SavedSearchConfig.class;
    }
}
