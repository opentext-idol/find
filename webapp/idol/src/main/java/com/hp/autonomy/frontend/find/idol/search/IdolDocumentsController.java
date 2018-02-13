/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndex;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequestBuilder;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import java.util.Optional;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
class IdolDocumentsController extends DocumentsController<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, String, IdolQueryRestrictions, IdolGetContentRequestIndex, IdolSearchResult, AciErrorException> {

    private final Integer documentSummaryMaxLength;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    public IdolDocumentsController(final IdolDocumentsService documentsService,
                                   final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                   final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory,
                                   final ObjectFactory<IdolSuggestRequestBuilder> suggestRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestBuilder> getContentRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory,
                                   final ConfigFileService<IdolFindConfig> configService) {
        super(documentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);

        this.documentSummaryMaxLength = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getDocumentSummaryMaxLength)
                .orElse(null);
    }

    @Override
    protected <T> T throwException(final String message) throws AciErrorException {
        throw new AciErrorException(message);
    }

    @Override
    protected void addParams(final GetContentRequestBuilder<IdolGetContentRequest, IdolGetContentRequestIndex, ?> request) {
        ((IdolGetContentRequestBuilder) request)
                .print(PrintParam.All);
    }

    @Override
    protected Integer getMaxSummaryCharacters() {
        return this.documentSummaryMaxLength;
    }
}
