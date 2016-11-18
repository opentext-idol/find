/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.SuggestResponseData;
import com.hp.autonomy.types.requests.qms.actions.query.params.QmsQueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
class FindQueryExecutor implements QueryExecutor {
    static final String MISSING_RULE_ERROR = "missing rule";
    private static final String INVALID_RULE_ERROR = "invalid rule";

    private final QueryExecutor queryExecutor;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired
    public FindQueryExecutor(
            @Qualifier(QUERY_EXECUTOR_BEAN_NAME)
            final QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public boolean performQuery(final QueryRequest.QueryType queryType) throws AciErrorException {
        return queryExecutor.performQuery(queryType);
    }

    @Override
    public QueryResponseData executeQuery(final AciParameters aciParameters, final QueryRequest.QueryType queryType) throws AciErrorException {
        QueryResponseData responseData;
        try {
            responseData = queryExecutor.executeQuery(aciParameters, queryType);
        } catch (final AciErrorException e) {
            final String errorString = e.getErrorString();
            if (MISSING_RULE_ERROR.equals(errorString) || INVALID_RULE_ERROR.equals(errorString)) {
                aciParameters.remove(QmsQueryParams.Blacklist.name());
                responseData = queryExecutor.executeQuery(aciParameters, queryType);
            } else {
                throw e;
            }
        }

        return responseData;
    }

    @Override
    public SuggestResponseData executeSuggest(final AciParameters aciParameters, final QueryRequest.QueryType queryType) throws AciErrorException {
        return queryExecutor.executeSuggest(aciParameters, queryType);
    }
}
