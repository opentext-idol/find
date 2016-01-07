/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web.test;

import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ConditionalOnProperty(name = "find.https.proxyHost")
public class HodTestConfiguration {
    // TODO: pull these out
    private static final String TEST_APP_API_KEY = "03e5efd3-4ddb-47ed-811c-f74c7198f1a8";
    private static final String TEST_APPLICATION = "Find_Test";
    private static final String TEST_DOMAIN = "c46dfa57-0d8e-4f0f-b419-e3acd0a482e9";

    @Autowired
    private TokenProxyService<EntityType.Combined, TokenType.Simple> tokenProxyService;

    @Autowired
    private HodServiceConfig<?, TokenType.Simple> hodServiceConfig;

    @PostConstruct
    public void init() throws HodErrorException {
        final AuthenticationService authenticationService = new AuthenticationServiceImpl(hodServiceConfig);
        final TokenProxy<EntityType.Application, TokenType.Simple> tokenProxy = authenticationService.authenticateApplication(new ApiKey(TEST_APP_API_KEY), TEST_APPLICATION, TEST_DOMAIN, TokenType.Simple.INSTANCE);

        final HodAuthentication authentication = mock(HodAuthentication.class);
        final HodAuthenticationPrincipal hodAuthenticationPrincipal = mock(HodAuthenticationPrincipal.class);
        final ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        when(identifier.toString()).thenReturn(TEST_APPLICATION);
        when(identifier.getDomain()).thenReturn(TEST_DOMAIN);
        when(hodAuthenticationPrincipal.getApplication()).thenReturn(identifier);
        when(authentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        //noinspection unchecked,rawtypes
        when(authentication.getTokenProxy()).thenReturn((TokenProxy) tokenProxy);

        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
