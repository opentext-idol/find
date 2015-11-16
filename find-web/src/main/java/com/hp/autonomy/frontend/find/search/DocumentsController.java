/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.beanconfiguration.HodCondition;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/api/public/search")
@Conditional(HodCondition.class) // TODO remove this
public class DocumentsController {
    @Autowired
    private DocumentsService documentsService;

    @RequestMapping(value = "query-text-index/results", method = RequestMethod.GET)
    @ResponseBody
    public Documents<FindDocument> query(
        @RequestParam("text") final String text,
        @RequestParam("max_results") final int maxResults,
        @RequestParam("summary") final Summary summary,
        @RequestParam("index") final List<ResourceIdentifier> index,
        @RequestParam(value = "field_text", defaultValue = "") final String fieldText,
        @RequestParam(value = "sort", required = false) final Sort sort,
        @RequestParam(value = "min_date", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
        @RequestParam(value = "max_date", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate
    ) throws HodErrorException {
        return documentsService.queryTextIndex(text, maxResults, summary, index, fieldText, sort, minDate, maxDate);
    }

    @RequestMapping(value="query-text-index/promotions", method = RequestMethod.GET)
    @ResponseBody
    public Documents<FindDocument> queryForPromotions(
            @RequestParam("text") final String text,
            @RequestParam("max_results") final int maxResults,
            @RequestParam("summary") final Summary summary,
            @RequestParam("index") final List<ResourceIdentifier> index,
            @RequestParam("field_text") final String fieldText,
            @RequestParam(value = "sort", required = false) final Sort sort,
            @RequestParam(value = "min_date", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) final DateTime minDate,
            @RequestParam(value = "max_date", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) final DateTime maxDate
    ) throws HodErrorException {
        return documentsService.queryTextIndexForPromotions(text, maxResults, summary, index, fieldText, sort, minDate, maxDate);
    }

    @RequestMapping(value = "similar-documents", method = RequestMethod.GET)
    @ResponseBody
    public List<FindDocument> findSimilar(
            @RequestParam("reference") final String reference,
            @RequestParam("indexes") final Set<ResourceIdentifier> indexes
    ) throws HodErrorException {
        return documentsService.findSimilar(indexes, reference);
    }
}
