/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.similar;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Document;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/public/search/similar-documents")
public class SimilarDocumentsController {
    @Autowired
    private SimilarDocumentsService similarDocumentsService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Document> findSimilar(
            @RequestParam("reference") final String reference,
            @RequestParam("indexes") final Set<ResourceIdentifier> indexes
    ) throws HodErrorException {
        return similarDocumentsService.findSimilar(indexes, reference);
    }
}
