/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/api/public/search")
public class DocumentsController<I extends Identifier, D extends FindDocument, E extends Exception> {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DocumentsService<I, D, E> documentsService;

    @RequestMapping(value = "query-text-index/results", method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> query(@RequestParam("queryParams") final FindQueryParams<I> findQueryParams) throws E {
        return documentsService.queryTextIndex(findQueryParams);
    }

    @RequestMapping(value = "query-text-index/promotions", method = RequestMethod.GET)
    @ResponseBody
    public Documents<D> queryForPromotions(@RequestParam("queryParams") final FindQueryParams<I> findQueryParams) throws E {
        return documentsService.queryTextIndexForPromotions(findQueryParams);
    }

    @RequestMapping(value = "similar-documents", method = RequestMethod.GET)
    @ResponseBody
    public List<D> findSimilar(@RequestParam("reference") final String reference, @RequestParam("indexes") final Set<I> indexes) throws E {
        return documentsService.findSimilar(indexes, reference);
    }
}
