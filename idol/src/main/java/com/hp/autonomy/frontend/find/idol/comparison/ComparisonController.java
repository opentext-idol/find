/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping(ComparisonController.BASE_PATH)
class ComparisonController<S extends Serializable, R extends SearchResult, E extends Exception> {
    static final String BASE_PATH = "/api/bi/comparison";
    static final String COMPARE_PATH = "compare";
    static final String RESULTS_PATH = "results";

    static final int STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    private static final String TEXT_PARAM = "text";
    static final String STATE_MATCH_PARAM = "state_match_ids";
    static final String STATE_DONT_MATCH_PARAM = "state_dont_match_ids";
    static final String RESULTS_START_PARAM = "start";
    static final String MAX_RESULTS_PARAM = "max_results";
    static final String SUMMARY_PARAM = "summary";
    static final String SORT_PARAM = "sort";
    static final String HIGHLIGHT_PARAM = "highlight";
    private static final String PROMOTIONS = "promotions";

    private final ComparisonService<R, E> comparisonService;
    private final DocumentsService<S, R, E> documentsService;

    @Autowired
    public ComparisonController(final ComparisonService<R, E> comparisonService, final DocumentsService<S, R, E> documentsService) {
        this.comparisonService = comparisonService;
        this.documentsService = documentsService;
    }

    @RequestMapping(value = COMPARE_PATH, method = RequestMethod.POST)
    public ComparisonStateTokens getCompareStateTokens(@RequestBody final ComparisonRequest<S> body) throws E {
        // If either query state token is null then try and fetch one using the query restrictions
        final String firstStateToken = body.getFirstQueryStateToken() != null ? body.getFirstQueryStateToken() : documentsService.getStateToken(body.getFirstRestrictions(), STATE_TOKEN_MAX_RESULTS, false);
        final String secondStateToken = body.getSecondQueryStateToken() != null ? body.getSecondQueryStateToken() : documentsService.getStateToken(body.getSecondRestrictions(), STATE_TOKEN_MAX_RESULTS, false);

        return comparisonService.getCompareStateTokens(firstStateToken, secondStateToken);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = RESULTS_PATH, method = RequestMethod.GET)
    public Documents<R> getResults(
            @RequestParam(STATE_MATCH_PARAM) final List<String> stateMatchIds,
            @RequestParam(value = STATE_DONT_MATCH_PARAM, required = false) final List<String> stateDontMatchIds,
            @RequestParam(value = TEXT_PARAM, required = false, defaultValue = "*") final String text,
            @RequestParam(value = RESULTS_START_PARAM, required = false, defaultValue = "1") final int resultsStart,
            @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
            @RequestParam(SUMMARY_PARAM) final String summary,
            @RequestParam(value = SORT_PARAM, required = false) final String sort,
            @RequestParam(value = HIGHLIGHT_PARAM, required = false, defaultValue = "true") final boolean highlight,
            @RequestParam(value = PROMOTIONS, defaultValue = "false") final boolean promotions
    ) throws E {
        return comparisonService.getResults(
                stateMatchIds,
                ListUtils.emptyIfNull(stateDontMatchIds),
                text,
                resultsStart,
                maxResults,
                summary,
                sort,
                highlight
        );
    }
}
