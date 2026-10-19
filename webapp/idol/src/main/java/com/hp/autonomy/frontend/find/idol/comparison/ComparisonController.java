/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.idol.api.ComparisonApi;
import com.hp.autonomy.frontend.find.idol.api.model.ComparisonStateTokens;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class ComparisonController implements ComparisonApi {
    private final ComparisonService<IdolSearchResult, AciErrorException> comparisonService;
    private final IdolDocumentsService documentsService;

    @Autowired
    public ComparisonController(final ComparisonService<IdolSearchResult, AciErrorException> comparisonService, final IdolDocumentsService documentsService) {
        this.comparisonService = comparisonService;
        this.documentsService = documentsService;
    }

    @Override
    public ResponseEntity<ComparisonStateTokens> compare(final ComparisonRequest<IdolQueryRestrictions> body) {
        final int STATE_TOKEN_MAX_RESULTS = comparisonService.getStateTokenMaxResults();
        // If either query state token is null then try and fetch one using the query restrictions
        final String firstStateToken = body.getFirstQueryStateToken() != null ? body.getFirstQueryStateToken() : documentsService.getStateToken(body.getFirstRestrictions(), STATE_TOKEN_MAX_RESULTS, QueryRequest.QueryType.MODIFIED, false);
        final String secondStateToken = body.getSecondQueryStateToken() != null ? body.getSecondQueryStateToken() : documentsService.getStateToken(body.getSecondRestrictions(), STATE_TOKEN_MAX_RESULTS, QueryRequest.QueryType.MODIFIED, false);

        return ResponseEntity.ok(comparisonService.getCompareStateTokens(firstStateToken, secondStateToken));
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<Documents<IdolSearchResult>> getComparisonResults(
            final List<String> stateMatchIds,
            final Integer maxResults,
            final String summary,
            final List<String> stateDontMatchIds,
            final String text,
            final String fieldText,
            final Integer resultsStart,
            final String sort,
            final Boolean highlight,
            final Boolean promotions
    ) {
        return ResponseEntity.ok(comparisonService.getResults(
                stateMatchIds,
                ListUtils.emptyIfNull(stateDontMatchIds),
                text,
                fieldText,
                resultsStart,
                maxResults,
                summary,
                sort,
                highlight
        ));
    }
}
