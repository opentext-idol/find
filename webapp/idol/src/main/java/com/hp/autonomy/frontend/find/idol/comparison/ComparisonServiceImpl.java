/*
 * Copyright 2016-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.requests.Documents;
import java.util.Optional;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@IdolService
public class ComparisonServiceImpl implements ComparisonService<IdolSearchResult, AciErrorException> {
    private final IdolDocumentsService documentsService;
    private final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;
    private final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory;

    private final int stateTokenMaxResults;
    private final Integer documentSummaryMaxLength;

    @Autowired
    public ComparisonServiceImpl(
            final IdolDocumentsService documentsService,
            final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
            final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory,
            final ConfigFileService<IdolFindConfig> configService
    ) {
        this.documentsService = documentsService;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
        this.queryRequestBuilderFactory = queryRequestBuilderFactory;
        this.stateTokenMaxResults = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getComparisonStoreStateMaxResults)
                .orElse(Integer.MAX_VALUE);
        this.documentSummaryMaxLength = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getDocumentSummaryMaxLength)
                .orElse(null);
    }

    private Documents<IdolSearchResult> getEmptyResults() {
        return new Documents<>(Collections.emptyList(), 0, "", null, null, null);
    }

    private String generateDifferenceStateToken(final String firstQueryStateToken, final String secondQueryStateToken) throws AciErrorException {
        final IdolQueryRestrictions queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                .queryText("*")
                .fieldText("")
                .minScore(0)
                .stateMatchId(firstQueryStateToken)
                .stateDontMatchId(secondQueryStateToken)
                .build();

        return documentsService.getStateToken(queryRestrictions, stateTokenMaxResults, false);
    }

    @Override
    public int getStateTokenMaxResults() {
        return stateTokenMaxResults;
    }

    @Override
    public Documents<IdolSearchResult> getResults(
            final List<String> stateMatchIds,
            final List<String> stateDontMatchIds,
            final String text,
            final String fieldText,
            final int resultsStart,
            final int maxResults,
            final String summary,
            final String sort,
            final boolean highlight
    ) throws AciErrorException {
        if (stateMatchIds.isEmpty()) {
            return getEmptyResults();
        }

        final IdolQueryRestrictions queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                .queryText(text)
                .fieldText(fieldText)
                .minScore(0)
                .stateMatchIds(stateMatchIds)
                .stateDontMatchIds(stateDontMatchIds)
                .build();

        final IdolQueryRequest queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(queryRestrictions)
                .start(resultsStart)
                .maxResults(maxResults)
                .summary(summary)
                .summaryCharacters(documentSummaryMaxLength)
                .sort(sort)
                .highlight(highlight)
                .autoCorrect(false)
                .queryType(QueryRequest.QueryType.RAW)
                .build();

        return documentsService.queryTextIndex(queryRequest);
    }

    @Override
    public ComparisonStateTokens getCompareStateTokens(final String firstStateToken, final String secondStateToken) throws AciErrorException {
        // First generate the relative complement state tokens, i.e. A \ B and B \ A
        // - where A is the set of documents in our "firstStateToken" and B is the set of
        // documents in our "secondStateToken".
        final String docsInFirstStateToken = generateDifferenceStateToken(firstStateToken, secondStateToken);
        final String docsInSecondStateToken = generateDifferenceStateToken(secondStateToken, firstStateToken);

        final ComparisonStateTokens stateTokens = new ComparisonStateTokens();
        stateTokens.setFirstQueryStateToken(firstStateToken);
        stateTokens.setSecondQueryStateToken(secondStateToken);
        stateTokens.setDocumentsOnlyInFirstStateToken(docsInFirstStateToken);
        stateTokens.setDocumentsOnlyInSecondStateToken(docsInSecondStateToken);

        return stateTokens;
    }
}
