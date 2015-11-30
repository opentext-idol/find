/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;

public class IdolFindConfigFileService extends AbstractAuthenticatingConfigFileService<IdolFindConfig> {
    @Override
    public void postInitialise(final IdolFindConfig config) {

    }

    @Override
    public Class<IdolFindConfig> getConfigClass() {
        return IdolFindConfig.class;
    }

    @Override
    public IdolFindConfig getEmptyConfig() {
        return new IdolFindConfig.Builder().build();
    }

    @Override
    public IdolFindConfig preUpdate(final IdolFindConfig config) {
        return config;
    }

    @Override
    public void postUpdate(final IdolFindConfig config) {

    }
}
