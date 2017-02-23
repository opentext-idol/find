/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileServiceTest;

public class HodFindConfigFileServiceTest extends FindConfigFileServiceTest<HodFindConfig, HodFindConfig.HodFindConfigBuilder> {
    @Override
    protected FindConfigFileService<HodFindConfig, HodFindConfig.HodFindConfigBuilder> constructConfigFileService() {
        return new HodFindConfigFileService(filterProvider, textEncryptor, fieldPathSerializer, fieldPathDeserializer);
    }

    @Override
    protected Class<HodFindConfig> getConfigClassType() {
        return HodFindConfig.class;
    }

    @Override
    protected void validateConfig(final String configFileContents) {
    }
}
