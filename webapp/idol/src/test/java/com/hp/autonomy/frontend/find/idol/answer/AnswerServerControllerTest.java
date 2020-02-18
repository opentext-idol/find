/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.search.*;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.answer.*;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.answer.params.ReportParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.autonomy.types.requests.idol.actions.answer.AnswerServerActions.Report;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnswerServerControllerTest {
    @Mock
    private AciService aciService;
    @Mock
    private AskAnswerServerService askAnswerServerService;
    @Mock
    private ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    @Mock
    private AskAnswerServerRequestBuilder requestBuilder;
    @Mock
    private ConfigService<IdolFindConfig> configService;
    @Mock
    private IdolFindConfig idolFindConfig;
    @Mock
    private AnswerServerConfig answerServerConfig;
    @Mock
    private ProcessorFactory processorFactory;
    @Mock
    private DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService;
    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;
    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;
    @Mock
    private ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory;
    @Mock
    private IdolQueryRequestBuilder queryRequestBuilder;

    private AnswerServerController controller;

    private ReportFact createFact(final String source) {
        final ReportFact fact = Mockito.mock(ReportFact.class);
        when(fact.getSource()).thenReturn(source);
        return fact;
    }

    private ReportItem createReport(final List<ReportFact> facts) {
        final ReportItem reportItem = new ReportItem();
        final ReportMetadata meta = Mockito.mock(ReportMetadata.class);
        reportItem.setMetadata(meta);
        when(meta.getFact()).thenReturn(facts);
        return reportItem;
    }

    private void mockReportResponse(final List<ReportItem> reportItems) {
        final ReportResponsedata reportResponse = new ReportResponsedata();
        final ReportItems reportResponseItems = Mockito.mock(ReportItems.class);
        when(reportResponseItems.getItem()).thenReturn(reportItems);
        reportResponse.setReport(reportResponseItems);
        when(aciService.executeAction(any(), any(), any())).thenReturn(reportResponse);
    }

    private IdolSearchResult createDoc(final String source) {
        return IdolSearchResult.builder().reference(source).build();
    }

    private void mockQueryResponse(final List<IdolSearchResult> docs) {
        final Documents<IdolSearchResult> docsResponse = Mockito.mock(Documents.class);
        when(docsResponse.getDocuments()).thenReturn(docs);
        when(documentsService.queryTextIndex(any())).thenReturn(docsResponse);
    }

    @Before
    public void setUp() {
        when(requestBuilderFactory.getObject()).thenReturn(requestBuilder);
        when(requestBuilder.text(any())).thenReturn(requestBuilder);
        when(requestBuilder.maxResults(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.proxiedParams(any())).thenReturn(requestBuilder);
        when(requestBuilder.systemNames(any())).thenReturn(requestBuilder);

        when(configService.getConfig()).thenReturn(idolFindConfig);
        when(idolFindConfig.getAnswerServer()).thenReturn(answerServerConfig);
        when(idolFindConfig.getReferenceField()).thenReturn("docref");

        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);

        when(queryRequestBuilderFactory.getObject()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryRestrictions(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.print(any())).thenReturn(queryRequestBuilder);

        mockReportResponse(Arrays.asList(
            createReport(Arrays.asList(createFact("source 1"), createFact("source 2"))),
            createReport(Collections.singletonList(createFact("source 3")))
        ));

        mockQueryResponse(Arrays.asList(
            createDoc("source 1"), createDoc("source 2"), createDoc("source 3")
        ));

        controller = new AnswerServerController(
            aciService, askAnswerServerService, requestBuilderFactory, configService,
            processorFactory, documentsService, queryRestrictionsBuilderFactory,
            queryRequestBuilderFactory);
    }

    @Test
    public void ask() {
        controller.ask("GPU", null,5);
        verify(askAnswerServerService).ask(any());
    }

    @Test
    public void getEntityFacts() {
        final List<ReportFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        final ArgumentCaptor<AciParameters> paramsCaptor =
            ArgumentCaptor.forClass(AciParameters.class);
        Mockito.verify(aciService).executeAction(any(), paramsCaptor.capture(), any());
        Assert.assertEquals("should send report action to answerserver", Report.name(),
            paramsCaptor.getValue().get(AciConstants.PARAM_ACTION));
        Assert.assertEquals("should send entity to answerserver",
            "space", paramsCaptor.getValue().get(ReportParams.Entity.name()));
        Assert.assertEquals("should send max results to answerserver",
            "7", paramsCaptor.getValue().get(ReportParams.MaxResults.name()));
        Assert.assertNull("should not send timeout to answerserver when not configured",
            paramsCaptor.getValue().get(ReportParams.Timeout.name()));

        Assert.assertEquals("should return all facts", 3, response.size());
        Assert.assertEquals("should return first fact", "source 1", response.get(0).getSource());
        Assert.assertEquals("should return second fact", "source 2", response.get(1).getSource());
        Assert.assertEquals("should return third fact", "source 3", response.get(2).getSource());
    }

    @Test
    public void getEntityFacts_noResult() {
        when(aciService.executeAction(any(), any(), any())).thenReturn(new ReportResponsedata());
        final List<ReportFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return empty list", Collections.emptyList(), response);
    }

    @Test
    public void getEntityFacts_noMaxResults() {
        final List<ReportFact> response =
            controller.getEntityFacts("space", null, Collections.singletonList("db"));

        final ArgumentCaptor<AciParameters> paramsCaptor =
            ArgumentCaptor.forClass(AciParameters.class);
        Mockito.verify(aciService).executeAction(any(), paramsCaptor.capture(), any());
        Assert.assertNull("should not send max results to answerserver",
            paramsCaptor.getValue().get(ReportParams.MaxResults.name()));
        Assert.assertEquals("should not limit results", 3, response.size());
    }

    @Test
    public void getEntityFacts_lowMaxResults() {
        final List<ReportFact> response =
            controller.getEntityFacts("space", 2, Collections.singletonList("db"));
        Assert.assertEquals("should limit results", 2, response.size());
        Assert.assertEquals("should return first fact", "source 1", response.get(0).getSource());
        Assert.assertEquals("should return second fact", "source 2", response.get(1).getSource());
    }

    @Test
    public void getEntityFacts_documentNotVisible() {
        mockQueryResponse(Arrays.asList(createDoc("source 1"), createDoc("source 3")));
        final List<ReportFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));
        Assert.assertEquals("should limit results", 2, response.size());
        Assert.assertEquals("should return first fact", "source 1", response.get(0).getSource());
        Assert.assertEquals("should return third fact", "source 3", response.get(1).getSource());
    }

}
