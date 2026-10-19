/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
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
import com.hp.autonomy.searchcomponents.core.view.ViewingPart;
import com.hp.autonomy.searchcomponents.idol.view.*;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.OutputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
        when(viewRequestBuilder.part(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.urlPrefix(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.subDocRef(any())).thenReturn(viewRequestBuilder);

        response = new MockHttpServletResponse();
        viewController = new IdolViewController(idolViewServerService, viewRequestBuilderFactory, controllerUtils, userService, authenticationInformationRetriever, configService, response);
        viewServerService = idolViewServerService;
        super.setUp();
    }

    @Override
    protected String getSampleDatabase() {
        return "SomeDatabase";
    }

    @Test
    public void testViewDocument() throws Exception {
        assertNull(viewController.viewDocument(getSampleDatabase(), ViewingPart.DOCUMENT,
            "SomeReference", null, null, null));

        verify(idolViewServerService).viewDocument(any(), any(OutputStream.class));
        assertEquals("text/html", response.getContentType());
    }

    @Test
    public void testViewStaticContentPromotion() throws Exception {
        assertNull(viewController.viewStaticContentPromotion("SomeReference"));

        verify(idolViewServerService).viewStaticContentPromotion(any(), any(OutputStream.class));
        assertEquals("text/html", response.getContentType());
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
