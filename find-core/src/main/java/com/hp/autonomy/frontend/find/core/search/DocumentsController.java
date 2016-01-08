/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.HavenDocument;
import com.hp.autonomy.searchcomponents.core.search.HavenQueryParams;
import com.hp.autonomy.types.requests.Documents;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
public abstract class DocumentsController<S extends Serializable, D extends HavenDocument, E extends Exception> {
    public static final String SEARCH_PATH = "/api/public/search";
    public static final String QUERY_PATH = "query-text-index/results";
    public static final String PROMOTIONS_PATH = "query-text-index/promotions";
    public static final String SIMILAR_DOCUMENTS_PATH = "similar-documents";

    public static final String TEXT_PARAM = "text";
    public static final String MAX_RESULTS_PARAM = "max_results";
    public static final String SUMMARY_PARAM = "summary";
    public static final String INDEX_PARAM = "index";
    public static final String FIELD_TEXT_PARAM = "field_text";
    public static final String SORT_PARAM = "sort";
    public static final String MIN_DATE_PARAM = "min_date";
    public static final String MAX_DATE_PARAM = "max_date";
    public static final String HIGHLIGHT_PARAM = "highlight";
    public static final String REFERENCE_PARAM = "reference";
    public static final String INDEXES_PARAM = "indexes";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected DocumentsService<S, D, E> documentsService;

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = QUERY_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> query(@RequestParam(TEXT_PARAM) final String text,
                              @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
                              @RequestParam(SUMMARY_PARAM) final String summary,
                              @RequestParam(INDEX_PARAM) final List<S> index,
                              @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
                              @RequestParam(value = SORT_PARAM, required = false) final String sort,
                              @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                              @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
                              @RequestParam(value = HIGHLIGHT_PARAM, required = false, defaultValue = "true") final boolean highlight) throws E {
        final HavenQueryParams<S> findQueryParams = new HavenQueryParams<>(text, maxResults, summary, index, fieldText, sort, minDate, maxDate, highlight);
        return documentsService.queryTextIndex(findQueryParams);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = PROMOTIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> queryForPromotions(@RequestParam(TEXT_PARAM) final String text,
                                           @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
                                           @RequestParam(SUMMARY_PARAM) final String summary,
                                           @RequestParam(INDEX_PARAM) final List<S> index,
                                           @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
                                           @RequestParam(value = SORT_PARAM, required = false) final String sort,
                                           @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                                           @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate,
                                           @RequestParam(value = HIGHLIGHT_PARAM, required = false, defaultValue = "true") final boolean highlight) throws E {
        final HavenQueryParams<S> findQueryParams = new HavenQueryParams<>(text, maxResults, summary, index, fieldText, sort, minDate, maxDate, highlight);
        return documentsService.queryTextIndexForPromotions(findQueryParams);
    }

    @RequestMapping(value = SIMILAR_DOCUMENTS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<D> findSimilar(@RequestParam(REFERENCE_PARAM) final String reference, @RequestParam(INDEXES_PARAM) final Set<S> indexes) throws E {
        return documentsService.findSimilar(indexes, reference);
    }
}
