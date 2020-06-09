/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest.QueryType;
import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
import com.hp.autonomy.types.idol.responses.GetQueryTagValuesResponseData;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.SuggestResponseData;
import com.hp.autonomy.types.requests.qms.actions.query.params.QmsQueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Primary
@Component
public class FindQueryExecutor implements QueryExecutor {
    static final String MISSING_RULE_ERROR = "missing rule";
    private static final String INVALID_RULE_ERROR = "invalid rule";

    private final QueryExecutor queryExecutor;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired
    public FindQueryExecutor(
            @Qualifier(QUERY_EXECUTOR_BEAN_NAME) final QueryExecutor queryExecutor
    ) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public boolean performQuery(final QueryType queryType) throws AciErrorException {
        return queryExecutor.performQuery(queryType);
    }

    @Override
    public QueryResponseData executeQuery(
            final AciParameters aciParameters,
            final QueryType queryType
    ) throws AciErrorException {
        return executeQueryDiscardingBlacklist(aciParameters, queryType, queryExecutor::executeQuery);
    }

    @Override
    public SuggestResponseData executeSuggest(
            final AciParameters aciParameters,
            final QueryType queryType
    ) throws AciErrorException {
        return queryExecutor.executeSuggest(aciParameters, queryType);
    }

    @Override
    public GetQueryTagValuesResponseData executeGetQueryTagValues(
            final AciParameters aciParameters,
            final QueryType queryType
    ) throws AciErrorException {
        return executeQueryDiscardingBlacklist(aciParameters, queryType, queryExecutor::executeGetQueryTagValues);
    }

    public static <R> R executeQueryDiscardingBlacklist(
            final AciParameters aciParameters,
            final QueryType queryType,
            final BiFunction<AciParameters, QueryType, R> function
    ) {
        try {
            return function.apply(aciParameters, queryType);
        } catch(final AciErrorException e) {
            final String errorString = e.getErrorString();

            if(MISSING_RULE_ERROR.equals(errorString) || INVALID_RULE_ERROR.equals(errorString)) {
                aciParameters.remove(QmsQueryParams.Blacklist.name());
                return function.apply(aciParameters, queryType);
            } else {
                throw e;
            }
        }
    }
}
