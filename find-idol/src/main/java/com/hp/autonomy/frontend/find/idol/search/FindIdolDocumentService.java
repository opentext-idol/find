/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.idol.configuration.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentService;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.requests.qms.actions.query.params.QmsQueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FindIdolDocumentService extends IdolDocumentService {
    static final String MISSING_RULE_ERROR = "missing rule";
    static final String INVALID_RULE_ERROR = "invalid rule";

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired
    public FindIdolDocumentService(final ConfigService<? extends HavenSearchCapable> configService, final HavenSearchAciParameterHandler parameterHandler, final AciService contentAciService, final AciService qmsAciService, final AciResponseJaxbProcessorFactory aciResponseProcessorFactory, final DatabasesService<Database, IdolDatabasesRequest, AciErrorException> databasesService) {
        super(configService, parameterHandler, contentAciService, qmsAciService, aciResponseProcessorFactory, databasesService);
    }

    @Override
    protected QueryResponseData executeQuery(final AciService aciService, final AciParameters aciParameters) {
        QueryResponseData responseData;
        try {
            responseData = aciService.executeAction(aciParameters, queryResponseProcessor);
        } catch (final AciErrorException e) {
            final String errorString = e.getErrorString();
            if (MISSING_RULE_ERROR.equals(errorString) || INVALID_RULE_ERROR.equals(errorString)) {
                aciParameters.remove(QmsQueryParams.Blacklist.name());
                responseData = aciService.executeAction(aciParameters, queryResponseProcessor);
            } else {
                throw e;
            }
        }

        return responseData;
    }
}
