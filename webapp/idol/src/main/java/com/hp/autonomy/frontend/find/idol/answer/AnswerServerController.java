/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import com.hp.autonomy.types.idol.responses.answer.ReportFact;
import com.hp.autonomy.types.idol.responses.answer.ReportResponsedata;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.answer.AnswerServerActions;
import com.hp.autonomy.types.requests.idol.actions.answer.params.ReportParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.find.core.search.DocumentsController.INDEXES_PARAM;

/**
 * These APIs should only be called when AnswerServer is enabled
 * ({@link com.hp.autonomy.frontend.find.core.web.MvcConstants.ANSWER_SERVER_ENABLED}).
 */
@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class AnswerServerController {
    static final String BASE_PATH = "/api/public/answer";
    static final String ASK_PATH = "ask";
    static final String TEXT_PARAM = "text";
    static final String ENTITY_PARAM = "entity";
    static final String FIELDTEXT_PARAM = "fieldText";
    static final String MAX_RESULTS_PARAM = "maxResults";

    private final AciService aciService;
    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final ConfigService<IdolFindConfig> configService;
    private final Processor<ReportResponsedata> reportProcessor;
    private final DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService;
    private final ObjectFactory<? extends QueryRestrictionsBuilder<IdolQueryRestrictions, String, ?>> queryRestrictionsBuilderFactory;
    private final ObjectFactory<? extends QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?>> queryRequestBuilderFactory;

    @Autowired
    AnswerServerController(
        final AciService aciService,
        final AskAnswerServerService askAnswerServerService,
        final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
        final ConfigService<IdolFindConfig> configService,
        final ProcessorFactory processorFactory,
        final DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService,
        final org.springframework.beans.factory.ObjectFactory<? extends QueryRestrictionsBuilder<IdolQueryRestrictions, String, ?>> queryRestrictionsBuilderFactory,
        final ObjectFactory<? extends QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?>> queryRequestBuilderFactory
    ) {
        this.aciService = aciService;
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.configService = configService;
        reportProcessor = processorFactory.getResponseDataProcessor(ReportResponsedata.class);
        this.documentsService = documentsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<AskAnswer> ask(@RequestParam(TEXT_PARAM)
                               final String text,
                               @RequestParam(value = FIELDTEXT_PARAM, required = false)
                               final String fieldText,
                               @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                               final Integer maxResults) {
        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .proxiedParams(StringUtils.isBlank(fieldText) ? Collections.emptyMap() : Collections.singletonMap("fieldtext", fieldText))
                .systemNames(configService.getConfig().getAnswerServer().getSystemNames())
                .build();

        return askAnswerServerService.ask(request);
    }

    /**
     * Retrieve facts involving a specific entity from AnswerServer.
     */
    @RequestMapping(value = "entity-facts", method = RequestMethod.GET)
    public List<ReportFact> getEntityFacts(
        @RequestParam(ENTITY_PARAM) final String entity,
        @RequestParam(value = MAX_RESULTS_PARAM, required = false) final Integer maxResults,
        @RequestParam(INDEXES_PARAM) final Collection<String> databases
    ) {
        final AciParameters params = new AciParameters(AnswerServerActions.Report.name());
        params.add(ReportParams.Entity.name(), entity);
        if (maxResults != null) {
            params.add(ReportParams.MaxResults.name(), maxResults);
        }

        final ReportResponsedata entityInfo = aciService.executeAction(
            configService.getConfig().getAnswerServer().toAciServerDetails(),
            params, reportProcessor);
        final List<ReportFact> allFacts;
        if (entityInfo.getReport() != null) {
            allFacts = entityInfo.getReport().getItem().stream()
                .flatMap(item -> item.getMetadata().getFact().stream())
                .limit(maxResults == null ? Long.MAX_VALUE : maxResults)
                .collect(Collectors.toList());
        } else {
            allFacts = Collections.emptyList();
        }

        // filter facts results to those extracted from visible documents
        final List<ReportFact> visibleFacts;
        if (allFacts.isEmpty()) {
            visibleFacts = allFacts;
        } else {
            final FieldText fieldText = new MATCH(
                configService.getConfig().getReferenceField(),
                allFacts.stream().map(ReportFact::getSource).collect(Collectors.toList()));
            final IdolQueryRestrictions queryRestrictions =
                queryRestrictionsBuilderFactory.getObject()
                    .fieldText(fieldText.toString())
                    .databases(databases)
                    .build();

            final IdolQueryRequest queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(queryRestrictions)
                .maxResults(allFacts.size())
                .queryType(QueryRequest.QueryType.RAW)
                .print(PrintParam.None.name())
                .build();

            final Documents<IdolSearchResult> docs = documentsService.queryTextIndex(queryRequest);
            final Set<String> visibleDocRefs = docs.getDocuments().stream()
                .map(doc -> doc.getReference())
                .collect(Collectors.toSet());
            visibleFacts = allFacts.stream()
                .filter(fact -> visibleDocRefs.contains(fact.getSource()))
                .collect(Collectors.toList());
        }

        return visibleFacts;
    }

}
