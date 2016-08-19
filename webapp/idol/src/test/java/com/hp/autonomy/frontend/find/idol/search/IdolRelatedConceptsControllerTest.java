/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsControllerTest;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilderFactory;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequest;
import com.hp.autonomy.types.idol.QsElement;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Mockito.when;

public class IdolRelatedConceptsControllerTest extends AbstractRelatedConceptsControllerTest<QsElement, IdolQueryRestrictions, IdolRelatedConceptsRequest, String, AciErrorException> {
    @Override
    protected RelatedConceptsController<QsElement, IdolQueryRestrictions, IdolRelatedConceptsRequest, String, AciErrorException> buildController(final RelatedConceptsService<QsElement, String, AciErrorException> relatedConceptsService, final QueryRestrictionsBuilderFactory<IdolQueryRestrictions, String> queryRestrictionsBuilderFactory, final ObjectFactory<RelatedConceptsRequest.Builder<IdolRelatedConceptsRequest, String>> relatedConceptsRequestBuilderFactory) {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new IdolQueryRestrictions.Builder());
        when(relatedConceptsRequestBuilderFactory.getObject()).thenReturn(new IdolRelatedConceptsRequest.Builder());
        return new IdolRelatedConceptsController(relatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }
}
