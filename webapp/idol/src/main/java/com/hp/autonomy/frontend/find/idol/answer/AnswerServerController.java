/*
 * Copyright 2017 Open Text.
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

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.ActionParameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.answer.AnswerServerActions;
import com.hp.autonomy.types.requests.idol.actions.answer.params.ReportParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import com.opentext.idol.types.marshalling.ProcessorFactory;
import com.opentext.idol.types.responses.answer.AskAnswer;
import com.opentext.idol.types.responses.answer.ReportFact;
import com.opentext.idol.types.responses.answer.ReportResponsedata;
import com.opentext.idol.types.responses.answer.System;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.*;
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
    private static final int DEFAULT_MAX_FACTS = 10;
    private static final String FACT_ID_FIELD = "FACTS/FACT_EXTRACT_/ID";
    private static final String FACT_SENTENCE_FIELD = "FACTS/FACT_EXTRACT_/SENTENCE";
    private static final ObjectMapper customizationDataObjectMapper = JsonMapper.builder().build();
    private static final Set<String> SECURED_SYSTEMS = Set.of("factbank", "passageextractor", "rag");

    private final AciService aciService;
    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final ConfigService<IdolFindConfig> configService;
    private final Processor<ReportResponsedata> reportProcessor;
    private final DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService;
    private final ObjectFactory<? extends QueryRestrictionsBuilder<IdolQueryRestrictions, String, ?>> queryRestrictionsBuilderFactory;
    private final ObjectFactory<? extends QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?>> queryRequestBuilderFactory;
    private final HavenSearchAciParameterHandler aciParameterHandler;
    private final Map<String, System> allSystems;

    @Autowired
    AnswerServerController(
        final AciService aciService,
        final AskAnswerServerService askAnswerServerService,
        final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
        final ConfigService<IdolFindConfig> configService,
        final ProcessorFactory processorFactory,
        final DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> documentsService,
        final org.springframework.beans.factory.ObjectFactory<? extends QueryRestrictionsBuilder<IdolQueryRestrictions, String, ?>> queryRestrictionsBuilderFactory,
        final ObjectFactory<? extends QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?>> queryRequestBuilderFactory,
        final HavenSearchAciParameterHandler aciParameterHandler
    ) {
        this.aciService = aciService;
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.configService = configService;
        reportProcessor = processorFactory.getResponseDataProcessor(ReportResponsedata.class);
        this.documentsService = documentsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
        this.aciParameterHandler = aciParameterHandler;

        allSystems = getAllSystems();
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<AskAnswer> ask(@RequestParam(TEXT_PARAM)
                               final String text,
                               @RequestParam(value = FIELDTEXT_PARAM, required = false)
                               final String fieldText,
                               @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                               final Integer maxResults) {
        final String customizationData;
        try {
            final List<Map<String, String>> systems = configService.getConfig().getAnswerServer().getSystemNames().stream()
                    .map(name -> {
                        final System system = allSystems.get(name);
                        if (system == null || !SECURED_SYSTEMS.contains(system.getType())) {
                            return null;
                        }
                        return Map.of(
                                "system_name", name,
                                "security_info", aciParameterHandler.getSecurityInfo()
                        );
                    })
                    .filter(s -> s != null)
                    .toList();

            customizationData = customizationDataObjectMapper.writeValueAsString(systems);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .proxiedParams(StringUtils.isBlank(fieldText) ? Collections.emptyMap() : Collections.singletonMap("fieldtext", fieldText))
                .systemNames(configService.getConfig().getAnswerServer().getSystemNames())
                .customizationData(customizationData)
                .build();

        return askAnswerServerService.ask(request);
    }

    /**
     * Retrieve facts involving a specific entity from AnswerServer.
     */
    @RequestMapping(value = "entity-facts", method = RequestMethod.GET)
    public List<SourcedFact> getEntityFacts(
        @RequestParam(ENTITY_PARAM) final String entity,
        @RequestParam(value = MAX_RESULTS_PARAM, required = false) final Integer maxResults,
        @RequestParam(INDEXES_PARAM) final Collection<String> databases
    ) {
        final ActionParameters params = new ActionParameters(AnswerServerActions.Report.name());
        params.add(ReportParams.Entity.name(), entity);
        if (maxResults != null) {
            params.add(ReportParams.MaxResults.name(), maxResults);
        }

        final ReportResponsedata entityInfo = aciService.executeAction(
            configService.getConfig().getAnswerServer().toAciServerDetails(),
            params, reportProcessor);
        final List<ReportFact> reportFacts;
        if (entityInfo.getReport() != null) {
            reportFacts = entityInfo.getReport().getItem().stream()
                .flatMap(item -> item.getMetadata().getFact().stream())
                .limit(maxResults == null ? DEFAULT_MAX_FACTS : maxResults)
                .collect(Collectors.toList());
        } else {
            reportFacts = Collections.emptyList();
        }

        // filter facts results to those extracted from visible documents
        // the backend may return the same fact multiple times (eg. with different dates)
        final Map<String, SourcedFact> sourcedFacts = new HashMap<>();
        if (!reportFacts.isEmpty()) {
            final FieldText fieldText = new MATCH(
                "*/" + FACT_ID_FIELD,
                reportFacts.stream().map(ReportFact::getSource).collect(Collectors.toList()));
            final IdolQueryRestrictions queryRestrictions =
                queryRestrictionsBuilderFactory.getObject()
                    .fieldText(fieldText.toString())
                    .databases(databases)
                    .build();

            final IdolQueryRequest queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(queryRestrictions)
                .maxResults(reportFacts.size() * 100) // expect no more than 100 documents per fact
                .queryType(QueryRequest.QueryType.RAW)
                .print(PrintParam.Fields.name())
                .printField(FACT_ID_FIELD)
                .printField(FACT_SENTENCE_FIELD)
                .referenceField(configService.getConfig().getReferenceField())
                .build();

            final Documents<IdolSearchResult> docs = documentsService.queryTextIndex(queryRequest);

            // get important parts from docs, indexing by fact ID
            final Map<String, List<DocumentFact>> docsByFactId = new HashMap<>();
            for (final IdolSearchResult doc : docs.getDocuments()) {
                for (final FieldValue<?> factsField : doc.getFieldMap().get("facts").getValues()) {
                    // unpack result of RecordType.parseValue
                    for (final Serializable factField :
                        ((Map<String, List<Serializable>>) factsField.getValue())
                            .get("fact_extract_")
                    ) {
                        final List<String> factIds = ((Map<String, List<String>>) factField)
                            .getOrDefault("id", Collections.emptyList());
                        final String sentence =
                            ((Map<String, List<String>>) factField).get("sentence").get(0);
                        for (final String factId : factIds) {
                            if (!docsByFactId.containsKey(factId)) {
                                docsByFactId.put(factId, new ArrayList<>());
                            }
                            docsByFactId.get(factId).add(
                                new DocumentFact(doc.getIndex(), doc.getReference(), sentence));
                        }
                    }
                }
            }

            for (final ReportFact reportFact : reportFacts) {
                if (docsByFactId.containsKey(reportFact.getSource())) {
                    sourcedFacts.put(reportFact.getSource(),
                        new SourcedFact(reportFact, docsByFactId.get(reportFact.getSource())));
                }
            }
        }

        return new ArrayList<>(sourcedFacts.values());
    }

    private Map<String, System> getAllSystems() {
        if (configService.getConfig().getAnswerServer().getEnabled()) {
            return askAnswerServerService.getStatus().getSystems().getSystem().stream()
                    .collect(Collectors.toMap(System::getName, s -> s));
        } else {
            return Map.of();
        }
    }


    /**
     * Reference to a document which is visible to the user and is a source for a fact.
     */
    public static class DocumentFact {
        /**
         * Database containing the document.
         */
        @JsonProperty
        public final String index;
        /**
         * Value for the Find-configured reference field.
         */
        @JsonProperty
        public final String reference;
        /**
         * Excerpt from the content that the fact was extracted from.
         */
        @JsonProperty
        public final String sentence;

        public DocumentFact(final String index, final String reference, final String sentence) {
            this.index = index;
            this.reference = reference;
            this.sentence = sentence;
        }

    }


    /**
     * A fact, along with its sources.
     */
    public static class SourcedFact {
        /**
         * The fact.
         */
        @JsonProperty
        public final ReportFact fact;
        /**
         * Non-empty list of documents which are sources for the fact.
         */
        @JsonProperty
        public final List<DocumentFact> documents;

        public SourcedFact(final ReportFact fact, final List<DocumentFact> documents) {
            this.fact = fact;
            this.documents = documents;
        }

    }

}
