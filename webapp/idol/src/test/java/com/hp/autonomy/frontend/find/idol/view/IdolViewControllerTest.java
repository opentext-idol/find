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

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciServiceException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerTest;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.view.ViewRequest;
import com.hp.autonomy.searchcomponents.idol.view.*;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewControllerTest extends AbstractViewControllerTest<IdolViewController, IdolViewRequest, String, AciErrorException> {
    @Mock
    private IdolViewServerService idolViewServerService;
    @Mock
    private ObjectFactory<IdolViewRequestBuilder> viewRequestBuilderFactory;
    @Mock
    private IdolViewRequestBuilder viewRequestBuilder;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;
    @Mock
    private ConfigFileService<IdolFindConfig> configService;

    @Override
    @Before
    public void setUp() {
        when(viewRequestBuilderFactory.getObject()).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.documentReference(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.database(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.highlightExpression(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.original(anyBoolean())).thenReturn(viewRequestBuilder);

        viewController = new IdolViewController(idolViewServerService, viewRequestBuilderFactory, controllerUtils, userService, authenticationInformationRetriever, configService);
        viewServerService = idolViewServerService;
        response = new MockHttpServletResponse();
        super.setUp();
    }

    @Override
    protected String getSampleDatabase() {
        return "SomeDatabase";
    }

    @Test
    public void viewDocumentNotFound() {
        assertNotNull(viewController.handleViewDocumentNotFoundException(new ViewDocumentNotFoundException("some reference"), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void viewServerError() {
        assertNotNull(viewController.handleViewServerErrorException(new ViewServerErrorException("some reference", new AciServiceException("It broke")), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }
}
