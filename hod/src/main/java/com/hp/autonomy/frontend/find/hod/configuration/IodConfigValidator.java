/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import com.hp.autonomy.frontend.find.hod.databases.FindHodDatabasesService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IodConfigValidator implements Validator<IodConfig> {

    private final FindHodDatabasesService findHodDatabasesService;
    private final AuthenticationService authenticationService;
    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public IodConfigValidator(final FindHodDatabasesService findHodDatabasesService, final AuthenticationService authenticationService, final ConfigService<HodFindConfig> configService) {
        this.findHodDatabasesService = findHodDatabasesService;
        this.authenticationService = authenticationService;
        this.configService = configService;
    }

    @Override
    public ValidationResult<?> validate(final IodConfig iodConfig) {
        return iodConfig.validate(findHodDatabasesService, authenticationService, configService.getConfig().getIod().getActiveIndexes());
    }

    @Override
    public Class<IodConfig> getSupportedClass() {
        return IodConfig.class;
    }
}
