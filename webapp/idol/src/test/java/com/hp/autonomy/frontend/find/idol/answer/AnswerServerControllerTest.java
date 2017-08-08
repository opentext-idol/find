/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.core.Authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnswerServerControllerTest {
    @Mock
    private AskAnswerServerService askAnswerServerService;
    @Mock
    private ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    @Mock
    private AskAnswerServerRequestBuilder requestBuilder;
    @Mock
    private AuthenticationInformationRetriever<Authentication, CommunityPrincipal> authenticationInformationRetriever;

    private AnswerServerController controller;

    @Before
    public void setUp() {
        when(requestBuilderFactory.getObject()).thenReturn(requestBuilder);
        when(requestBuilder.text(any())).thenReturn(requestBuilder);
        when(requestBuilder.maxResults(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.customizationData(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.proxiedParams(anyMap())).thenReturn(requestBuilder);

        controller = new AnswerServerController(askAnswerServerService, requestBuilderFactory, authenticationInformationRetriever, null);
    }

    @Test
    public void ask() throws JsonProcessingException {
        controller.ask("GPU", 5);
        verify(askAnswerServerService).ask(any());
    }
}
