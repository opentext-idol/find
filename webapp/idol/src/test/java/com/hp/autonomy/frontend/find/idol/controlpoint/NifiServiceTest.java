/*
 * Copyright 2021 Open Text.
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

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.ActionParameter;
import com.autonomy.aci.client.transport.impl.AciResponseInputStreamImpl;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.idol.configuration.NifiConfig;
import com.hp.autonomy.frontend.find.idol.nifi.NifiAction;
import com.hp.autonomy.frontend.find.idol.nifi.NifiService;
import com.opentext.idol.types.marshalling.ProcessorFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class NifiServiceTest {
    private static final ServerConfig serverConfig = ServerConfig.builder()
        .protocol(AciServerDetails.TransportProtocol.HTTP).host("test").port(123).build();
    public static final NifiConfig validConfig = NifiConfig.builder()
        .enabled(true).server(serverConfig).listAction("get_actions").build();
    public static final String listActionsResponse = "" +
        "<actions>" +
        "    <action><id>first_action</id><displayName>Name 1</displayName></action>" +
        "    <action><id>get_actions</id><displayName>List Actions</displayName></action>" +
        "    <action><id>second_action</id><displayName>Name 2</displayName></action>" +
        "</actions>";
    private ProcessorFactory processorFactory;
    private AciService aciService;
    private Function<String, String> request;

    public static AciResponseInputStream buildResponse(final String body) throws Exception {
        final HttpResponse res = new BasicHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        res.setEntity(new StringEntity(body, ContentType.APPLICATION_XML));
        return new AciResponseInputStreamImpl(res);
    }

    @Before
    public void setUp() {
        processorFactory = Mockito.mock(ProcessorFactory.class);
        Mockito.doReturn(Mockito.mock(Processor.class)).when(processorFactory).getVoidProcessor();

        aciService = Mockito.mock(AciService.class);
        Mockito.doAnswer(inv -> {
            final String action = ((Set<ActionParameter<String>>) inv.getArgument(1, Set.class))
                .stream().filter(p -> p.getName().equals("action")).findFirst().get().getValue();
            return inv.getArgument(2, Processor.class)
                .process(buildResponse(request.apply(action)));
        }).when(aciService).executeAction(any(), any(), any());

        request = Mockito.mock(Function.class);
        Mockito.doReturn(listActionsResponse).when(request).apply("get_actions");
    }

    @Test
    public void testCheckStatus() {
        new NifiService(processorFactory, aciService, validConfig).checkStatus();

        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", "get_actions"));
        params.add(new AciParameter("userRoles", ""));
        Mockito.verify(aciService)
            .executeAction(eq(serverConfig.toAciServerDetails()), eq(params), any());
    }

    @Test(expected = AciServiceException.class)
    public void testCheckStatus_error() {
        Mockito.doThrow(new AciServiceException("error")).when(request).apply("get_actions");
        new NifiService(processorFactory, aciService, validConfig).checkStatus();
    }

    @Test
    public void testGetActions() {
        final List<NifiAction> actions = new NifiService(processorFactory, aciService, validConfig)
            .getActions("the_user", Arrays.asList("role1", "role2"));

        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", "get_actions"));
        params.add(new AciParameter("username", "the_user"));
        params.add(new AciParameter("userRoles", "role1,role2"));
        Mockito.verify(aciService)
            .executeAction(eq(serverConfig.toAciServerDetails()), eq(params), any());

        Assert.assertEquals(Arrays.asList(
            new NifiAction("first_action", "Name 1"),
            new NifiAction("second_action", "Name 2")
        ), actions);
    }

    @Test
    public void testExecuteAction() {
        Mockito.doReturn("").when(request).apply("the_action");
        new NifiService(processorFactory, aciService, validConfig).executeAction(
            "the_action", "token", "secInfo", null, Arrays.asList("role1", "role2"), null, null);

        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", "the_action"));
        params.add(new AciParameter("stateMatchId", "token"));
        params.add(new AciParameter("securityInfo", "secInfo"));
        params.add(new AciParameter("userRoles", "role1,role2"));
        Mockito.verify(aciService)
            .executeAction(eq(serverConfig.toAciServerDetails()), eq(params), any());
    }

    @Test
    public void testExecuteAction_allParams() {
        Mockito.doReturn("").when(request).apply("the_action");
        new NifiService(processorFactory, aciService, validConfig).executeAction(
            "the_action", "token", "secInfo", "the_user", Arrays.asList("role1", "role2"),
            "search 123", "reason");

        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", "the_action"));
        params.add(new AciParameter("stateMatchId", "token"));
        params.add(new AciParameter("securityInfo", "secInfo"));
        params.add(new AciParameter("username", "the_user"));
        params.add(new AciParameter("userRoles", "role1,role2"));
        params.add(new AciParameter("searchName", "search 123"));
        params.add(new AciParameter("label", "reason"));
        Mockito.verify(aciService)
            .executeAction(eq(serverConfig.toAciServerDetails()), eq(params), any());
    }

}
