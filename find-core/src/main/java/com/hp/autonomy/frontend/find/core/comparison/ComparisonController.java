/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(ComparisonController.BASE_PATH)
public class ComparisonController<S extends Serializable, R extends SearchResult, E extends Exception> {
    public static final String BASE_PATH = "/api/public/comparison";
    public static final String COMPARE_PATH = "compare";
    public static final String RESULTS_PATH = "results";

    public static final int STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    public static final String TEXT_PARAM = "text";
    public static final String STATE_MATCH_PARAM = "state_match_ids";
    public static final String STATE_DONT_MATCH_PARAM = "state_dont_match_ids";
    public static final String RESULTS_START_PARAM = "start";
    public static final String MAX_RESULTS_PARAM = "max_results";
    public static final String SUMMARY_PARAM = "summary";
    public static final String SORT_PARAM = "sort";
    public static final String HIGHLIGHT_PARAM = "highlight";

    private ComparisonService<R, E> comparisonService;
    private DocumentsService<S, R, E> documentsService;

    @Autowired
    public ComparisonController(final ComparisonService<R, E> comparisonService, final DocumentsService<S, R, E> documentsService) {
        this.comparisonService = comparisonService;
        this.documentsService = documentsService;
    }

    @RequestMapping(value = COMPARE_PATH, method = RequestMethod.POST)
    public ComparisonStateTokens getCompareStateTokens(@RequestBody final ComparisonRequest<S> body) throws E {
        // If either query state token is null then try and fetch one using the query restrictions
        final String firstStateToken = body.getFirstQueryStateToken() != null ? body.getFirstQueryStateToken() : documentsService.getStateToken(body.getFirstRestrictions(), STATE_TOKEN_MAX_RESULTS);
        final String secondStateToken = body.getSecondQueryStateToken() != null ? body.getSecondQueryStateToken() : documentsService.getStateToken(body.getSecondRestrictions(), STATE_TOKEN_MAX_RESULTS);

        return comparisonService.getCompareStateTokens(firstStateToken, secondStateToken);
    }

    @RequestMapping(value = RESULTS_PATH, method = RequestMethod.GET)
    public Documents<R> getResults(
            @RequestParam(value = STATE_MATCH_PARAM) final List<String> stateMatchIds,
            @RequestParam(value = STATE_DONT_MATCH_PARAM, required = false) final List<String> stateDontMatchIds,
            @RequestParam(value = TEXT_PARAM, required = false, defaultValue = "*") final String text,
            @RequestParam(value = RESULTS_START_PARAM, required = false, defaultValue = "1") final int resultsStart,
            @RequestParam(MAX_RESULTS_PARAM) final int maxResults,
            @RequestParam(SUMMARY_PARAM) final String summary,
            @RequestParam(value = SORT_PARAM, required = false) final String sort,
            @RequestParam(value = HIGHLIGHT_PARAM, required = false, defaultValue = "true") final boolean highlight
    ) throws E {
        return comparisonService.getResults(
                stateMatchIds,
                stateDontMatchIds == null ? Collections.<String>emptyList() : stateDontMatchIds,
                text,
                resultsStart,
                maxResults,
                summary,
                sort,
                highlight
        );
    }
}
