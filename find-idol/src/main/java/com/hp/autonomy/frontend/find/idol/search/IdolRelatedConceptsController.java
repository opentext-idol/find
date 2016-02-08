/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequest;
import com.hp.autonomy.types.idol.QsElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
public class IdolRelatedConceptsController extends RelatedConceptsController<QsElement, String, AciErrorException> {

    public static final int QUERY_SUMMARY_LENGTH = 50;

    @Autowired
    public IdolRelatedConceptsController(final RelatedConceptsService<QsElement, String, AciErrorException> relatedConceptsService, final QueryRestrictionsBuilder<String> queryRestrictionsBuilder) {
        super(relatedConceptsService, queryRestrictionsBuilder);
    }

    @Override
    protected IdolRelatedConceptsRequest buildRelatedConceptsRequest(final QueryRestrictions<String> queryRestrictions) {
        final IdolRelatedConceptsRequest idolRelatedConceptsRequest = new IdolRelatedConceptsRequest();
        idolRelatedConceptsRequest.setQueryRestrictions(queryRestrictions);
        idolRelatedConceptsRequest.setQuerySummaryLength(QUERY_SUMMARY_LENGTH);
        return idolRelatedConceptsRequest;
    }
}
