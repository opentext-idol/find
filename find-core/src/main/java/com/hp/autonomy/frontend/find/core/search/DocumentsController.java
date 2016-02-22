/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequest;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestIndex;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
public abstract class DocumentsController<S extends Serializable, R extends SearchResult, E extends Exception> {
    public static final String SEARCH_PATH = "/api/public/search";
    public static final String QUERY_PATH = "query-text-index/results";
    public static final String PROMOTIONS_PATH = "query-text-index/promotions";
    public static final String SIMILAR_DOCUMENTS_PATH = "similar-documents";
    public static final String GET_DOCUMENT_CONTENT_PATH = "get-document-content";

    public static final String TEXT_PARAM = "text";
    public static final String RESULTS_START_PARAM = "start";
    public static final String MAX_RESULTS_PARAM = "max_results";
    public static final String SUMMARY_PARAM = "summary";
    public static final String INDEXES_PARAM = "indexes";
    public static final String FIELD_TEXT_PARAM = "field_text";
    public static final String SORT_PARAM = "sort";
    public static final String MIN_DATE_PARAM = "min_date";
    public static final String MAX_DATE_PARAM = "max_date";
    public static final String HIGHLIGHT_PARAM = "highlight";
    public static final String REFERENCE_PARAM = "reference";
    public static final String AUTO_CORRECT_PARAM = "auto_correct";
    public static final String DATABASE_PARAM = "database";

    public static final int MAX_SUMMARY_CHARACTERS = 250;

    public static final int FIND_SIMILAR_MAX_RESULTS = 3;

    protected final DocumentsService<S, R, E> documentsService;
    protected final QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    protected DocumentsController(final DocumentsService<S, R, E> documentsService, final QueryRestrictionsBuilder<S> queryRestrictionsBuilder) {
        this.documentsService = documentsService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
    }

    protected abstract <T> T throwException(final String message) throws E;

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = QUERY_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<R> query(@RequestParam(TEXT_PARAM) final String text,
                              @RequestParam(value = RESULTS_START_PARAM, defaultValue = "1") final int resultsStart,
                              @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
                              @RequestParam(SUMMARY_PARAM) final String summary,
                              @RequestParam(INDEXES_PARAM) final List<S> index,
                              @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
                              @RequestParam(value = SORT_PARAM, required = false) final String sort,
                              @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                              @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
                              @RequestParam(value = HIGHLIGHT_PARAM, defaultValue = "true") final boolean highlight,
                              @RequestParam(value = AUTO_CORRECT_PARAM, defaultValue = "true") final boolean autoCorrect) throws E {
        final SearchRequest<S> searchRequest = parseRequestParamsToObject(text, resultsStart, maxResults, summary, index, fieldText, sort, minDate, maxDate, highlight, autoCorrect);
        return documentsService.queryTextIndex(searchRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = PROMOTIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<R> queryForPromotions(@RequestParam(TEXT_PARAM) final String text,
                                           @RequestParam(value = RESULTS_START_PARAM, defaultValue = "1") final int resultsStart,
                                           @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
                                           @RequestParam(SUMMARY_PARAM) final String summary,
                                           @RequestParam(INDEXES_PARAM) final List<S> index,
                                           @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
                                           @RequestParam(value = SORT_PARAM, required = false) final String sort,
                                           @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                                           @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
                                           @RequestParam(value = HIGHLIGHT_PARAM, defaultValue = "true") final boolean highlight,
                                           @RequestParam(value = AUTO_CORRECT_PARAM, defaultValue = "true") final boolean autoCorrect) throws E {
        final SearchRequest<S> searchRequest = parseRequestParamsToObject(text, resultsStart, maxResults, summary, index, fieldText, sort, minDate, maxDate, highlight, autoCorrect);
        return documentsService.queryTextIndexForPromotions(searchRequest);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    protected SearchRequest<S> parseRequestParamsToObject(final String text, final int resultsStart, final int maxResults, final String summary, final List<S> databases, final String fieldText, final String sort, final DateTime minDate, final DateTime maxDate, final boolean highlight, final boolean autoCorrect) {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build(text, fieldText, databases, minDate, maxDate, Collections.<String>emptyList(), Collections.<String>emptyList());
        return new SearchRequest<>(queryRestrictions, resultsStart, maxResults, summary, MAX_SUMMARY_CHARACTERS, sort, highlight, autoCorrect, null);
    }

    @RequestMapping(value = SIMILAR_DOCUMENTS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<R> findSimilar(@RequestParam(REFERENCE_PARAM) final String reference, @RequestParam(INDEXES_PARAM) final List<S> indexes) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build(null, null, indexes != null ? indexes : Collections.<S>emptyList(), null, null, Collections.<String>emptyList(), Collections.<String>emptyList());
        final SuggestRequest<S> suggestRequest = new SuggestRequest<>();
        suggestRequest.setQueryRestrictions(queryRestrictions);
        suggestRequest.setReference(reference);
        suggestRequest.setSummary("concept");
        suggestRequest.setMaxResults(FIND_SIMILAR_MAX_RESULTS);
        return documentsService.findSimilar(suggestRequest);
    }

    @RequestMapping(value = GET_DOCUMENT_CONTENT_PATH, method = RequestMethod.GET)
    @ResponseBody
    public R getDocumentContent(@RequestParam(REFERENCE_PARAM) final String reference,
                                @RequestParam(DATABASE_PARAM) final S database) throws E {
        final GetContentRequestIndex<S> getContentRequestIndex = new GetContentRequestIndex<>(database, Collections.singleton(reference));
        final GetContentRequest<S> getContentRequest = new GetContentRequest<>(Collections.singleton(getContentRequestIndex), PrintParam.All.name());
        final List<R> results = documentsService.getDocumentContent(getContentRequest);

        return results.isEmpty() ? this.<R>throwException("No content found for document with reference " + reference + " in database " + database) : results.get(0);
    }
}
