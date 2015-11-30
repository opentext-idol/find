/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import com.hp.autonomy.frontend.find.hod.indexes.HodIndexesService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IodConfigValidator implements Validator<IodConfig> {

    @Autowired
    private HodIndexesService hodIndexesService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public ValidationResult<?> validate(final IodConfig iodConfig) {
        return iodConfig.validate(hodIndexesService, authenticationService);
    }

    @Override
    public Class<IodConfig> getSupportedClass() {
        return IodConfig.class;
    }
}
