/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigApiKeyService implements ApiKeyService {

    @Autowired
    private ConfigService<FindConfig> configService;

    @Override
    public String getApiKey() {
        return configService.getConfig().getIod().getApiKey();
    }
}
