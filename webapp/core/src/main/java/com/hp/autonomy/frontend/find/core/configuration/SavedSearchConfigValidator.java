/*
 * Copyright 2015-2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class SavedSearchConfigValidator implements Validator<SavedSearchConfig> {
    @Override
    public ValidationResult<Void> validate(final SavedSearchConfig config) {
        boolean valid = true;
        if(config.getPollForUpdates() != null && config.getPollForUpdates() && (config.getPollingInterval() == null || config.getPollingInterval() < 0)) {
            valid = false;
        }

        return new ValidationResult<>(valid);
    }

    @Override
    public Class<SavedSearchConfig> getSupportedClass() {
        return SavedSearchConfig.class;
    }
}
