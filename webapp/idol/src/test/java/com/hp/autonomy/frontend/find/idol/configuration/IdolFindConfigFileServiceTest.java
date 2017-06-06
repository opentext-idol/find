/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileServiceTest;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class IdolFindConfigFileServiceTest extends FindConfigFileServiceTest<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {

    @MockBean
    private Flyway flyway;

    @MockBean
    private AciHttpClient aciHttpClient;

    @MockBean
    private ProcessorFactory processorFactory;

    @Mock
    private IdolFindConfig config;

    @Mock
    private CommunityAuthentication community;

    @SuppressWarnings("rawtypes")
    @Mock
    private ValidationResult validationResult;

    @Mock
    private AciServerDetails serverDetails;

    @Override
    protected FindConfigFileService<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> constructConfigFileService() {
        return new IdolFindConfigFileService(
                filterProvider,
                textEncryptor,
                fieldPathSerializer,
                fieldPathDeserializer,
                aciHttpClient,
                processorFactory,
                flyway);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(config.getLogin()).thenReturn(community);
        when(config.getCommunityDetails()).thenReturn(serverDetails);
        //noinspection unchecked
        when(community.validate(any(AciServiceImpl.class), any(ProcessorFactory.class))).thenReturn(validationResult);

        when(serverDetails.getProtocol()).thenReturn(AciServerDetails.TransportProtocol.HTTP);
        when(serverDetails.getHost()).thenReturn("communityHost");
        when(serverDetails.getPort()).thenReturn(9000);
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
        when(validationResult.isValid()).thenReturn(true);

        findConfigFileService.postUpdate(config);

        verify(flyway, times(1)).migrate();

        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_HOST), is(nullValue()));
        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_PORT), is(nullValue()));
        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_PROTOCOL), is(nullValue()));
    }

    @Test
    public void postUpdateNoCommunity() {
        when(validationResult.isValid()).thenReturn(false);

        findConfigFileService.postUpdate(config);

        verify(flyway, times(0)).migrate();

        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_HOST), is(nullValue()));
        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_PORT), is(nullValue()));
        assertThat(System.getProperty(IdolFindConfigFileService.COMMUNITY_PROTOCOL), is(nullValue()));
    }
}
