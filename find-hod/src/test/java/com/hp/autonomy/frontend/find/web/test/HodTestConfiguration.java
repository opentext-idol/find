/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.SingleUserAuthentication;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfigFileService;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ConditionalOnProperty(name = "find.https.proxyHost")
public class HodTestConfiguration {
    @Value("${find.test.api.key}")
    private String apiKey;
    @Value("${find.test.application}")
    private String application;
    @Value("${find.test.domain}")
    private String domain;

    @Autowired
    private TokenProxyService<EntityType.Combined, TokenType.Simple> tokenProxyService;

    @Autowired
    private HodServiceConfig<?, TokenType.Simple> hodServiceConfig;

    public static void writeConfigFile(final String directory) throws IOException {
        final SingleUserAuthentication login = new SingleUserAuthentication.Builder().setMethod("singleUser").setSingleUser(new BCryptUsernameAndPassword.Builder().setUsername("admin").build()).build();
        final IodConfig iodConfig = new IodConfig.Builder().setApiKey(System.getProperty("find.test.api.key")).setPublicIndexesEnabled(true).build();
        final HodFindConfig hodFindConfig = new HodFindConfig.Builder().setLogin(login).setIod(iodConfig).setAllowedOrigins(Collections.singleton("http://localhost:8080")).build();
        new ObjectMapper().writeValue(new File(directory, "config.json"), hodFindConfig);
    }

    @PostConstruct
    public void init() throws HodErrorException {
        final AuthenticationService authenticationService = new AuthenticationServiceImpl(hodServiceConfig);
        final TokenProxy<EntityType.Application, TokenType.Simple> tokenProxy = authenticationService.authenticateApplication(new ApiKey(apiKey), application, domain, TokenType.Simple.INSTANCE);

        final HodAuthentication authentication = mock(HodAuthentication.class);
        final HodAuthenticationPrincipal hodAuthenticationPrincipal = mock(HodAuthenticationPrincipal.class);
        final ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        when(identifier.toString()).thenReturn(application);
        when(identifier.getDomain()).thenReturn(domain);
        when(hodAuthenticationPrincipal.getApplication()).thenReturn(identifier);
        when(authentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        //noinspection unchecked,rawtypes
        when(authentication.getTokenProxy()).thenReturn((TokenProxy) tokenProxy);

        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
