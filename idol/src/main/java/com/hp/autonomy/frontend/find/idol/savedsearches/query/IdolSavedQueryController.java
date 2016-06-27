/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilderFactory;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
class IdolSavedQueryController extends SavedQueryController<String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> {
    @Autowired
    public IdolSavedQueryController(final SavedSearchService<SavedQuery> service,
                                    final DocumentsService<String, IdolSearchResult, AciErrorException> documentsService,
                                    final FieldTextParser fieldTextParser,
                                    final QueryRestrictionsBuilderFactory<IdolQueryRestrictions, String> queryRestrictionsBuilderFactory) {
        super(service, documentsService, fieldTextParser, queryRestrictionsBuilderFactory);
    }

    @Override
    protected String convertEmbeddableIndex(final EmbeddableIndex embeddableIndex) {
        return embeddableIndex.getName();
    }

    @Override
    protected String getNoResultsPrintParam() {
        return PrintParam.NoResults.name();
    }
}
