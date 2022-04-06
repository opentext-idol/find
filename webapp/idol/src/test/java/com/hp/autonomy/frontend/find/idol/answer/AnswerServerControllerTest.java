/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
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

import java.io.Serializable;
import java.util.*;

import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.DocumentFact;
import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.SourcedFact;
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

    private Serializable createFactField(final String sentence, final List<String> sources) {
        final HashMap<String, List<String>> fact = new HashMap<>();
        fact.put("sentence", new ArrayList<>(Collections.singletonList(sentence)));
        fact.put("id", new ArrayList<>(sources));
        return fact;
    }

    private IdolSearchResult createDoc(
        final String reference, final List<Serializable> factFields
    ) {
        return IdolSearchResult.builder()
            .index("db")
            .reference(reference)
            .fieldEntry("facts", FieldInfo.builder()
                .value(new FieldValue<Serializable>(new HashMap<>(Collections.singletonMap(
                    "fact_extract_",
                    new ArrayList<>(factFields))), "a load of facts"
                ))
                .build())
            .build();
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
        when(queryRequestBuilder.printField(any())).thenReturn(queryRequestBuilder);

        mockReportResponse(Collections.singletonList(
            createReport(Arrays.asList(createFact("1"), createFact("2"), createFact("3")))
        ));
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Arrays.asList("1", "2", "3"))
            ))
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
    public void getEntityFacts_singleResult() {
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Collections.singletonList("1"))
            ))
        ));
        final List<SourcedFact> response =
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

        Assert.assertEquals("should return matching fact", 1, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
        Assert.assertEquals("fact should include matching document",
            1, response.get(0).documents.size());
        final DocumentFact doc = response.get(0).documents.get(0);
        Assert.assertEquals("should return document index", "db", doc.index);
        Assert.assertEquals("should return document ref", "doc 1", doc.reference);
        Assert.assertEquals("should return document sentence", "first fact", doc.sentence);
    }

    @Test
    public void getEntityFacts_docWithMultipleSentences() {
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Arrays.asList(
                createFactField("first fact", Collections.singletonList("1")),
                createFactField("second fact", Collections.singletonList("2"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return both facts", 2, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
        Assert.assertEquals("first fact should include document",
            1, response.get(0).documents.size());
        Assert.assertEquals("first fact document should be correct",
            "doc 1", response.get(0).documents.get(0).reference);
        Assert.assertEquals("should return second fact", "2", response.get(1).fact.getSource());
        Assert.assertEquals("second fact should include document",
            1, response.get(1).documents.size());
        Assert.assertEquals("second fact document should be correct",
            "doc 1", response.get(1).documents.get(0).reference);
    }

    @Test
    public void getEntityFacts_sentenceWithMultipleFacts() {
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Arrays.asList("1", "2"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return both facts", 2, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
        Assert.assertEquals("first fact should include document",
            1, response.get(0).documents.size());
        Assert.assertEquals("should return second fact", "2", response.get(1).fact.getSource());
        Assert.assertEquals("second fact should include document",
            1, response.get(1).documents.size());
    }

    @Test
    public void getEntityFacts_docWithSameFactTwice() {
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Arrays.asList(
                createFactField("first fact", Collections.singletonList("1")),
                createFactField("first fact again", Collections.singletonList("1"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return the fact", 1, response.size());
        Assert.assertEquals("fact should include the document twice",
            2, response.get(0).documents.size());
        Assert.assertEquals("first reference should be correct",
            "doc 1", response.get(0).documents.get(0).reference);
        Assert.assertEquals("first sentence should be correct",
            "first fact", response.get(0).documents.get(0).sentence);
        Assert.assertEquals("second reference should be correct",
            "doc 1", response.get(0).documents.get(1).reference);
        Assert.assertEquals("second sentence should be correct",
            "first fact again", response.get(0).documents.get(1).sentence);
    }

    @Test
    public void getEntityFacts_multipleDocs() {
        mockQueryResponse(Arrays.asList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Collections.singletonList("1"))
            )),
            createDoc("doc 2", Collections.singletonList(
                createFactField("second fact", Collections.singletonList("2"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return both facts", 2, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
        Assert.assertEquals("first fact should include document",
            1, response.get(0).documents.size());
        Assert.assertEquals("first fact document should be correct",
            "doc 1", response.get(0).documents.get(0).reference);
        Assert.assertEquals("should return second fact", "2", response.get(1).fact.getSource());
        Assert.assertEquals("second fact should include document",
            1, response.get(1).documents.size());
        Assert.assertEquals("second fact document should be correct",
            "doc 2", response.get(1).documents.get(0).reference);
    }

    @Test
    public void getEntityFacts_multipleDocsWithSameFact() {
        mockQueryResponse(Arrays.asList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Collections.singletonList("1"))
            )),
            createDoc("doc 2", Collections.singletonList(
                createFactField("first fact again", Collections.singletonList("1"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return the fact", 1, response.size());
        Assert.assertEquals("fact should include both documents",
            2, response.get(0).documents.size());
        Assert.assertEquals("first document should be correct",
            "doc 1", response.get(0).documents.get(0).reference);
        Assert.assertEquals("second document should be correct",
            "doc 2", response.get(0).documents.get(1).reference);
    }

    @Test
    public void getEntityFacts_multipleReports() {
        mockReportResponse(Arrays.asList(
            createReport(Collections.singletonList(createFact("1"))),
            createReport(Collections.singletonList(createFact("2")))
        ));
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Arrays.asList(
                createFactField("first fact", Collections.singletonList("1")),
                createFactField("second fact", Collections.singletonList("2"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return both facts", 2, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
        Assert.assertEquals("should return second fact", "2", response.get(1).fact.getSource());
    }

    @Test
    public void getEntityFacts_duplicateReports() {
        mockReportResponse(Arrays.asList(
            createReport(Collections.singletonList(createFact("1"))),
            createReport(Collections.singletonList(createFact("1")))
        ));
        mockQueryResponse(Collections.singletonList(
            createDoc("doc 1", Collections.singletonList(
                createFactField("first fact", Collections.singletonList("1"))
            ))
        ));
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return the fact only once", 1, response.size());
        Assert.assertEquals("should return first fact", "1", response.get(0).fact.getSource());
    }

    @Test
    public void getEntityFacts_noFacts() {
        // no ReportItems - different from empty ReportItems
        when(aciService.executeAction(any(), any(), any())).thenReturn(new ReportResponsedata());
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 7, Collections.singletonList("db"));

        Assert.assertEquals("should return empty list", Collections.emptyList(), response);
    }

    @Test
    public void getEntityFacts_noMaxResults() {
        final List<SourcedFact> response =
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
        final List<SourcedFact> response =
            controller.getEntityFacts("space", 2, Collections.singletonList("db"));
        Assert.assertEquals("should limit results", 2, response.size());
        Assert.assertEquals("should return first fact", "1",
            response.get(0).fact.getSource());
        Assert.assertEquals("should return second fact", "2",
            response.get(1).fact.getSource());
    }

}
