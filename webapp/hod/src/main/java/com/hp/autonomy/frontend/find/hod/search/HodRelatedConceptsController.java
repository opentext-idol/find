/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.api.textindex.query.search.Entity;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
class HodRelatedConceptsController extends RelatedConceptsController<Entity, HodQueryRestrictions, HodRelatedConceptsRequest, ResourceName, HodErrorException> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodRelatedConceptsController(final HodRelatedConceptsService relatedConceptsService,
                                        final ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                        final ObjectFactory<HodRelatedConceptsRequestBuilder> relatedConceptsRequestBuilderFactory) {
        super(relatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }
}
