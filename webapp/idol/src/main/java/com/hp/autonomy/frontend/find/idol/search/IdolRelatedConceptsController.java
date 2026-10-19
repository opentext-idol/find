/*
 * Copyright 2015 Open Text.
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

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsResponse;
import com.hp.autonomy.frontend.find.idol.api.RelatedConceptsApi;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.*;
import com.opentext.idol.types.responses.QsElement;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
class IdolRelatedConceptsController extends RelatedConceptsController<QsElement, IdolQueryRestrictions, IdolRelatedConceptsRequest, String, AciErrorException> implements RelatedConceptsApi {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolRelatedConceptsController(final IdolRelatedConceptsService relatedConceptsService,
                                         final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                         final ObjectFactory<IdolRelatedConceptsRequestBuilder> relatedConceptsRequestBuilderFactory) {
        super(relatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<RelatedConceptsResponse<QsElement>> findRelatedConcepts(
            final String queryText,
            final List<String> databases,
            final String fieldText,
            final ZonedDateTime minDate,
            final ZonedDateTime maxDate,
            final Integer minScore,
            final List<String> stateMatchTokens,
            final List<String> stateDontMatchTokens,
            final Integer maxResults,
            final QueryRequest.QueryType queryType
    ) {
        return ResponseEntity.ok(super.findRelatedConcepts(queryText, fieldText, databases,
                minDate, maxDate, minScore, stateMatchTokens, stateDontMatchTokens, maxResults,
                queryType));
    }
}
