/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
public class IdolDocumentsController extends DocumentsController<String, SearchResult, AciErrorException> {
    @Autowired
    public IdolDocumentsController(final DocumentsService<String, SearchResult, AciErrorException> documentsService, final QueryRestrictionsBuilder<String> queryRestrictionsBuilder) {
        super(documentsService, queryRestrictionsBuilder);
    }
}
