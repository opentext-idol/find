/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsService;
import com.hp.autonomy.types.idol.responses.QsElement;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
class IdolRelatedConceptsController extends RelatedConceptsController<QsElement, IdolQueryRestrictions, IdolRelatedConceptsRequest, String, AciErrorException> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolRelatedConceptsController(final IdolRelatedConceptsService relatedConceptsService,
                                         final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                         final ObjectFactory<IdolRelatedConceptsRequestBuilder> relatedConceptsRequestBuilderFactory) {
        super(relatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }
}
