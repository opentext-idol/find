/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
class IdolSavedQueryController extends SavedQueryController<IdolQueryRequest, String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolSavedQueryController(final SavedSearchService<SavedQuery, SavedQuery.Builder> service,
                                    final IdolDocumentsService documentsService,
                                    final FieldTextParser fieldTextParser,
                                    final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                    final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory) {
        super(service, documentsService, fieldTextParser, queryRestrictionsBuilderFactory, queryRequestBuilderFactory);
    }

    @Override
    protected String convertEmbeddableIndex(final EmbeddableIndex embeddableIndex) {
        return embeddableIndex.getName();
    }

    @Override
    protected void addParams(final QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?> queryRequestBuilder) {
        queryRequestBuilder.print(PrintParam.NoResults.name());
    }
}
