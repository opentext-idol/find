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
import com.hp.autonomy.searchcomponents.idol.configuration.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentService;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.idol.Hit;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.qms.actions.query.params.QmsQueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindIdolDocumentService extends IdolDocumentService {
    static final String MISSING_RULE_ERROR = "missing rule";
    static final String INVALID_RULE_ERROR = "invalid rule";

    @Autowired
    public FindIdolDocumentService(final ConfigService<? extends HavenSearchCapable> configService, final HavenSearchAciParameterHandler parameterHandler, final AciService contentAciService, final AciService qmsAciService, final AciResponseJaxbProcessorFactory aciResponseProcessorFactory) {
        super(configService, parameterHandler, contentAciService, qmsAciService, aciResponseProcessorFactory);
    }

    @Override
    protected Documents<IdolSearchResult> executeQuery(final AciService aciService, final AciParameters aciParameters, final boolean autoCorrect) {
        QueryResponseData responseData;
        try {
            responseData = aciService.executeAction(aciParameters, queryResponseProcessor);
        } catch (final AciErrorException e) {
            final String errorString = e.getErrorString();
            if (MISSING_RULE_ERROR.equals(errorString) || INVALID_RULE_ERROR.equals(errorString)) {
                aciParameters.remove(QmsQueryParams.Blacklist.name());
                responseData = aciService.executeAction(aciParameters, queryResponseProcessor);
            }
            else {
                throw e;
            }
        }

        final List<Hit> hits = responseData.getHit();
        final String spellingQuery = responseData.getSpellingquery();

        // If IDOL has a spelling suggestion, retry query for auto correct
        final Documents<IdolSearchResult> documents;
        if (autoCorrect && spellingQuery != null) {
            documents = rerunQueryWithAdjustedSpelling(aciService, aciParameters, responseData, spellingQuery);
        } else {
            final List<IdolSearchResult> results = parseQueryHits(hits);
            documents = new Documents<>(results, responseData.getTotalhits(), null, null, null);
        }

        return documents;
    }
}
