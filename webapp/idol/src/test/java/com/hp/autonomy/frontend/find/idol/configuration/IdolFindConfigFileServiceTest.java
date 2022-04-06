/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileServiceTest;
import com.hp.autonomy.frontend.find.idol.beanconfiguration.IdolConfigUpdateHandler;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldPathNormaliserImpl;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class IdolFindConfigFileServiceTest extends FindConfigFileServiceTest<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {

    @MockBean
    private IdolConfigUpdateHandler configUpdateHandler;

    @MockBean
    private IdolFieldPathNormaliserImpl idolFieldPathNormaliser;

    @Mock
    private IdolFindConfig config;

    @Override
    protected FindConfigFileService<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> constructConfigFileService() {
        return new IdolFindConfigFileService(
                filterProvider,
                textEncryptor,
                fieldPathSerializer,
                fieldPathDeserializer,
                configUpdateHandler,
                idolFieldPathNormaliser);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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

    @Override
    public void postUpdate() {
        findConfigFileService.postUpdate(config);

        verify(configUpdateHandler, times(1)).update(config);
    }
}
