/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

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
@RequestMapping("/api/public/search")
public abstract class DocumentsController<S extends Serializable, D extends FindDocument, E extends Exception> {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected DocumentsService<S, D, E> documentsService;

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = "query-text-index/results", method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> query(@RequestParam("text") final String text,
                              @RequestParam("max_results") final int maxResults,
                              @RequestParam("summary") final String summary,
                              @RequestParam("index") final List<S> index,
                              @RequestParam(value = "field_text", defaultValue = "") final String fieldText,
                              @RequestParam(value = "sort", required = false) final String sort,
                              @RequestParam(value = "min_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                              @RequestParam(value = "max_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate) throws E {
        final FindQueryParams<S> findQueryParams = new FindQueryParams<>(text, maxResults, summary, index, fieldText, sort, minDate, maxDate);
        return documentsService.queryTextIndex(findQueryParams);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = "query-text-index/promotions", method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> queryForPromotions(@RequestParam("text") final String text,
                                           @RequestParam("max_results") final int maxResults,
                                           @RequestParam("summary") final String summary,
                                           @RequestParam("index") final List<S> index,
                                           @RequestParam(value = "field_text", defaultValue = "") final String fieldText,
                                           @RequestParam(value = "sort", required = false) final String sort,
                                           @RequestParam(value = "min_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
                                           @RequestParam(value = "max_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate) throws E {
        final FindQueryParams<S> findQueryParams = new FindQueryParams<>(text, maxResults, summary, index, fieldText, sort, minDate, maxDate);
        return documentsService.queryTextIndexForPromotions(findQueryParams);
    }

    @RequestMapping(value = "similar-documents", method = RequestMethod.GET)
    @ResponseBody
    public List<D> findSimilar(@RequestParam("reference") final String reference, @RequestParam("indexes") final Set<S> indexes) throws E {
        return documentsService.findSimilar(indexes, reference);
    }
}
