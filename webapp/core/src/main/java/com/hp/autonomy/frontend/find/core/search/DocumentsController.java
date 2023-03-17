/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.database.Databases;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequest;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestIndex;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequestBuilder;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.Hit;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.TermGetBestResponseData;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import com.hp.autonomy.types.requests.idol.actions.term.TermActions;
import com.hp.autonomy.types.requests.idol.actions.term.params.TermGetBestParams;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
public abstract class DocumentsController<RQ extends QueryRequest<Q>, RS extends SuggestRequest<Q>, RC extends GetContentRequest<T>, S extends Serializable, Q extends QueryRestrictions<S>, T extends GetContentRequestIndex<S>, R extends SearchResult, E extends Exception> {
    public static final String SEARCH_PATH = "/api/public/search";
    public static final String QUERY_PATH = "query-text-index/results";
    public static final String TEXT_PARAM = "text";
    public static final String RESULTS_START_PARAM = "start";
    public static final String MAX_RESULTS_PARAM = "max_results";
    public static final String SUMMARY_PARAM = "summary";
    public static final String INDEXES_PARAM = "indexes";
    static final String SIMILAR_DOCUMENTS_PATH = "similar-documents";
    static final String GET_DOCUMENT_CONTENT_PATH = "get-document-content";
    static final String REFERENCE_PARAM = "reference";
    protected static final String AUTO_CORRECT_PARAM = "auto_correct";
    protected static final String INTENT_BASED_RANKING_PARAM = "intentBasedRanking";
    protected static final String QUERY_TYPE_PARAM = "queryType";
    protected static final String DATABASE_PARAM = "database";
    protected static final String FIELD_TEXT_PARAM = "field_text";
    protected static final String SORT_PARAM = "sort";
    protected static final String MIN_DATE_PARAM = "min_date";
    protected static final String MAX_DATE_PARAM = "max_date";
    protected static final String HIGHLIGHT_PARAM = "highlight";
    protected static final String MIN_SCORE_PARAM = "min_score";

    protected final AciService crosslingualAciService;
    protected final DocumentsService<RQ, RS, RC, Q, R, E> documentsService;
    protected final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory;
    protected final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory;
    private final ObjectFactory<? extends SuggestRequestBuilder<RS, Q, ?>> suggestRequestBuilderFactory;
    private final ObjectFactory<? extends GetContentRequestBuilder<RC, T, ?>> getContentRequestBuilderFactory;
    private final ObjectFactory<? extends GetContentRequestIndexBuilder<T, S, ?>> getContentRequestIndexBuilderFactory;
    private final Processor<QueryResponseData> queryResponseProcessor;
    private final Processor<TermGetBestResponseData> termGetBestResponseProcessor;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    protected DocumentsController(final AciService crosslingualAciService,
                                  final ProcessorFactory processorFactory,
                                  final DocumentsService<RQ, RS, RC, Q, R, E> documentsService,
                                  final ObjectFactory<? extends QueryRestrictionsBuilder<Q, S, ?>> queryRestrictionsBuilderFactory,
                                  final ObjectFactory<? extends QueryRequestBuilder<RQ, Q, ?>> queryRequestBuilderFactory,
                                  final ObjectFactory<? extends SuggestRequestBuilder<RS, Q, ?>> suggestRequestBuilderFactory,
                                  final ObjectFactory<? extends GetContentRequestBuilder<RC, T, ?>> getContentRequestBuilderFactory,
                                  final ObjectFactory<? extends GetContentRequestIndexBuilder<T, S, ?>> getContentRequestIndexBuilderFactory) {
        this.crosslingualAciService = crosslingualAciService;
        this.documentsService = documentsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
        this.suggestRequestBuilderFactory = suggestRequestBuilderFactory;
        this.getContentRequestBuilderFactory = getContentRequestBuilderFactory;
        this.getContentRequestIndexBuilderFactory = getContentRequestIndexBuilderFactory;
        queryResponseProcessor = processorFactory.getResponseDataProcessor(QueryResponseData.class);
        termGetBestResponseProcessor =
            processorFactory.getResponseDataProcessor(TermGetBestResponseData.class);
    }

    protected abstract <EX> EX throwException(final String message) throws E;

    protected abstract void addParams(final GetContentRequestBuilder<RC, T, ?> request);

    protected abstract String getFieldValue(final R doc, final String fieldName);

    protected abstract void addClIndexParams(final AciParameters params, final Q queryRestrictions);

    protected abstract S getClDbName();

    protected Integer getMaxSummaryCharacters() {
        return 250;
    }

    protected abstract String getReferenceField();

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = QUERY_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<R> query(
        @RequestParam(TEXT_PARAM) final String queryText,
        @RequestParam(value = RESULTS_START_PARAM, defaultValue = "1") final int resultsStart,
        @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
        @RequestParam(SUMMARY_PARAM) final String summary,
        @RequestParam(value = INDEXES_PARAM, required = false) final List<S> databases,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(value = SORT_PARAM, required = false) final String sort,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = HIGHLIGHT_PARAM, defaultValue = "true") final boolean highlight,
        @RequestParam(value = MIN_SCORE_PARAM, defaultValue = "0") final int minScore,
        @RequestParam(value = AUTO_CORRECT_PARAM, defaultValue = "true") final boolean autoCorrect,
        @RequestParam(value = INTENT_BASED_RANKING_PARAM, defaultValue = "false") final boolean intentBasedRanking,
        @RequestParam(value = QUERY_TYPE_PARAM, defaultValue = "MODIFIED") final String queryType,
        @RequestParam(value = "crosslingualOntology", defaultValue = "false") final boolean crosslingualOntology,
        @RequestParam(value = "crosslingualIndex", defaultValue = "false") final boolean crosslingualIndex
    ) throws E {
        if (crosslingualIndex) {
            final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                .queryText(queryText)
                .fieldText(fieldText)
                .databases(Collections.singletonList(getClDbName()))
                .minDate(minDate)
                .maxDate(maxDate)
                .minScore(minScore)
                .build();
            final AciParameters qParams = new AciParameters(QueryActions.Query.name());
            addClIndexParams(qParams, queryRestrictions);
            qParams.add(QueryParams.MaxResults.name(), 6);
            final List<Hit> docs =
                crosslingualAciService.executeAction(qParams, queryResponseProcessor).getHits();
            if (docs.isEmpty()) {
                return new Documents<>(Collections.emptyList(), 0, queryText, null, null, null, null);
            }

            final AciParameters tgbParams = new AciParameters(TermActions.TermGetBest.name());
            tgbParams.put(TermGetBestParams.Reference.name(),
                docs.stream()
                    .map(doc -> {
                        try {
                            return URLEncoder.encode(
                                doc.getReference(), StandardCharsets.UTF_8.name());
                        } catch (final UnsupportedEncodingException e) {
                            return null;
                        }
                    })
                    .filter(term -> term != null)
                    .collect(Collectors.joining("+")));

            final StringBuilder clText = new StringBuilder();
            for (final TermGetBestResponseData.Term term :
                crosslingualAciService.executeAction(tgbParams, termGetBestResponseProcessor).getTerm()
            ) {
                clText.append(term.getValue());
                clText.append("~[");
                clText.append(term.getApcmWeight());
                clText.append("] ");
            }

            final Q clRes = queryRestrictionsBuilderFactory.getObject()
                .queryText(clText.toString())
                .databases(ListUtils.emptyIfNull(databases))
                .build();

            final RQ clReq = queryRequestBuilderFactory.getObject()
                .queryRestrictions(clRes)
                .start(resultsStart)
                .maxResults(maxResults)
                .summaryCharacters(getMaxSummaryCharacters())
                .highlight(highlight)
                .summary(summary)
                .sort(sort)
                .queryType(QueryRequest.QueryType.valueOf(queryType))
                .intentBasedRanking(intentBasedRanking)
                .build();

            return documentsService.queryTextIndex(clReq);

        } else {
            final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                .queryText(queryText)
                .fieldText(fieldText)
                .databases(ListUtils.emptyIfNull(databases))
                .minDate(minDate)
                .maxDate(maxDate)
                .minScore(minScore)
                .build();

            final RQ queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(queryRestrictions)
                .start(crosslingualOntology ? 1 : resultsStart)
                .maxResults(crosslingualOntology ? 1 : maxResults)
                .summaryCharacters(getMaxSummaryCharacters())
                .highlight(highlight)
                .autoCorrect(autoCorrect)
                .summary(summary)
                .sort(crosslingualOntology ? null : sort)
                .queryType(QueryRequest.QueryType.valueOf(queryType))
                .intentBasedRanking(intentBasedRanking)
                .referenceField(getReferenceField())
                .build();
            final Documents<R> docs = documentsService.queryTextIndex(queryRequest);

            if (crosslingualOntology) {
                if (docs.getDocuments().isEmpty()) {
                    return new Documents<>(Collections.emptyList(), 0, queryText, null, null, null, null);
                }

                final String pageFieldName = "wikipedia_eng";
                final String pageName = getFieldValue(docs.getDocuments().get(0), pageFieldName);
                if (pageName == null) {
                    return docs;
                }

                final Q clRes = queryRestrictionsBuilderFactory.getObject()
                    .queryText("*")
                    .fieldText(new MATCH(pageFieldName, pageName).toString())
                    .databases(ListUtils.emptyIfNull(databases))
                    .build();

                final RQ clReq = queryRequestBuilderFactory.getObject()
                    .queryRestrictions(clRes)
                    .start(resultsStart)
                    .maxResults(maxResults)
                    .summaryCharacters(getMaxSummaryCharacters())
                    .highlight(highlight)
                    .summary(summary)
                    .sort(sort)
                    .queryType(QueryRequest.QueryType.valueOf(queryType))
                    .intentBasedRanking(intentBasedRanking)
                    .referenceField(getReferenceField())
                    .build();

                return documentsService.queryTextIndex(clReq);

            } else {
                return docs;
            }
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = SIMILAR_DOCUMENTS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<R> findSimilar(
        @RequestParam(REFERENCE_PARAM) final String reference,
        @RequestParam(value = RESULTS_START_PARAM, defaultValue = "1") final int resultsStart,
        @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
        @RequestParam(SUMMARY_PARAM) final String summary,
        @RequestParam(value = INDEXES_PARAM, required = false) final List<S> databases,
        @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
        @RequestParam(value = SORT_PARAM, required = false) final String sort,
        @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
        @RequestParam(value = HIGHLIGHT_PARAM, defaultValue = "true") final boolean highlight,
        @RequestParam(value = MIN_SCORE_PARAM, defaultValue = "0") final int minScore
    ) throws E {
        final Q queryRestrictions = queryRestrictionsBuilderFactory.getObject()
            .fieldText(fieldText)
            .databases(ListUtils.emptyIfNull(databases))
            .minDate(minDate)
            .maxDate(maxDate)
            .minScore(minScore)
            .build();

        final RS suggestRequest = suggestRequestBuilderFactory.getObject()
            .reference(reference)
            .queryRestrictions(queryRestrictions)
            .start(resultsStart)
            .maxResults(maxResults)
            .summaryCharacters(getMaxSummaryCharacters())
            .highlight(highlight)
            .summary(summary)
            .sort(sort)
            .referenceField(getReferenceField())
            .build();

        return documentsService.findSimilar(suggestRequest);
    }

    @RequestMapping(value = GET_DOCUMENT_CONTENT_PATH, method = RequestMethod.GET)
    @ResponseBody
    public R getDocumentContent(
        @RequestParam(REFERENCE_PARAM) final String reference,
        @RequestParam(DATABASE_PARAM) final S database
    ) throws E {
        final T getContentRequestIndex = getContentRequestIndexBuilderFactory.getObject()
            .index(database)
            .reference(reference)
            .build();
        final GetContentRequestBuilder<RC, T, ?> requestBuilder = getContentRequestBuilderFactory.getObject()
            .indexAndReferences(getContentRequestIndex)
            .referenceField(getReferenceField());
        addParams(requestBuilder);
        final RC getContentRequest = requestBuilder.build();

        final List<R> results = documentsService.getDocumentContent(getContentRequest);
        return results.isEmpty()
            ? throwException("No content found for document with reference " + reference + " in database " + database)
            : results.get(0);
    }
}
