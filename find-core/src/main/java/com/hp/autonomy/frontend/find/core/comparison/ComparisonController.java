/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
public class ComparisonController<S extends Serializable, R extends SearchResult, E extends Exception> {
    public static final String PATH = "/api/public/compare";
    public static final int STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

    private ComparisonService<R, E> comparisonService;
    private DocumentsService<S, R, E> documentsService;

    @Autowired
    public ComparisonController(final ComparisonService<R, E> comparisonService, final DocumentsService<S, R, E> documentsService) {
        this.comparisonService = comparisonService;
        this.documentsService = documentsService;
    }

    @RequestMapping(value = PATH, method = RequestMethod.POST)
    public Comparison<R> compare(@RequestBody final ComparisonRequest<S> body) throws E {
        if(body.getFirstDifferenceToken() != null && body.getSecondDifferenceToken() != null) {

            // If we have both difference state tokens, then we should have been passed the query state tokens also
            if(body.getFirstQueryToken() == null || body.getSecondQueryToken() == null) {
                throw new IllegalArgumentException("Query state tokens cannot be null if defining difference state tokens.");
            }

            return comparisonService.compareStateTokens(body.getFirstQueryToken(), body.getSecondQueryToken(), body.getFirstDifferenceToken(), body.getSecondDifferenceToken(), body.getResultsStart(), body.getMaxResults(), body.getSummary(), body.getSort(), body.isHighlight());
        } else {
            // If either query state token is null then try and fetch one using the query restrictions
            final String firstStateToken = body.getFirstQueryToken() != null ? body.getFirstQueryToken() : documentsService.getStateToken(body.getFirstRestrictions(), STATE_TOKEN_MAX_RESULTS);
            final String secondStateToken = body.getSecondQueryToken() != null ? body.getSecondQueryToken() : documentsService.getStateToken(body.getSecondRestrictions(), STATE_TOKEN_MAX_RESULTS);

            return comparisonService.compareStateTokens(firstStateToken, secondStateToken, body.getResultsStart(), body.getMaxResults(), body.getSummary(), body.getSort(), body.isHighlight());
        }
    }
}
