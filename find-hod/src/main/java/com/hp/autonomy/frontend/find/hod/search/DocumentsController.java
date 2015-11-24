/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.HodCondition;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
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
    public Documents<FindDocument> query(@RequestParam("queryParams") final QueryParams queryParams) throws HodErrorException {
        return documentsService.queryTextIndex(queryParams);
    }

    @RequestMapping(value = "query-text-index/promotions", method = RequestMethod.GET)
    @ResponseBody
    public Documents<FindDocument> queryForPromotions(@RequestParam("queryParams") final QueryParams queryParams) throws HodErrorException {
        return documentsService.queryTextIndexForPromotions(queryParams);
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
