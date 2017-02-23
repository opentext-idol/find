/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileServiceTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class IdolFindConfigFileServiceTest extends FindConfigFileServiceTest<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {
    @Override
    protected FindConfigFileService<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> constructConfigFileService() {
        return new IdolFindConfigFileService(filterProvider, textEncryptor, fieldPathSerializer, fieldPathDeserializer);
    }

    @Override
    protected Class<IdolFindConfig> getConfigClassType() {
        return IdolFindConfig.class;
    }

    @Override
    protected void validateConfig(final String configFileContents) {
        assertThat(configFileContents, not(containsString("indexProtocol")));
        assertThat(configFileContents, not(containsString("indexPort")));
        assertThat(configFileContents, not(containsString("serviceProtocol")));
        assertThat(configFileContents, not(containsString("servicePort")));
        assertThat(configFileContents, not(containsString("productType")));
        assertThat(configFileContents, not(containsString("indexErrorMessage")));
        assertThat(configFileContents, not(containsString("productTypeRegex")));
    }
}
