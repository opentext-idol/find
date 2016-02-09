/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
public class HodDocumentsController extends DocumentsController<ResourceIdentifier, HodSearchResult, HodErrorException> {
    @Autowired
    public HodDocumentsController(final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService, final QueryRestrictionsBuilder<ResourceIdentifier> queryRestrictionsBuilder) {
        super(documentsService, queryRestrictionsBuilder);
    }

    @Override
    protected <T> T throwException(final String message) throws HodErrorException {
        throw new HodErrorException(new HodError.Builder().setMessage(message).build(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
