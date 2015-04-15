/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.iod.client.api.search.Documents;
import com.hp.autonomy.iod.client.api.search.Summary;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/search/query-text-index")
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Documents query(
        @RequestParam("text") final String text,
        @RequestParam("max_results") final int maxResults,
        @RequestParam("summary") final Summary summary,
        @RequestParam("index") final List<String> index
    ) throws IodErrorException {
        return documentsService.queryTextIndex(text, maxResults, summary, index);
    }
}
