/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;

import static org.mockito.Mockito.when;

public class HodSavedSearchQueryControllerTest extends SavedQueryControllerTest<ResourceIdentifier, HodQueryRestrictions, HodSearchResult, HodErrorException> {
    @Override
    protected SavedQueryController<ResourceIdentifier, HodQueryRestrictions, HodSearchResult, HodErrorException> constructController() {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new HodQueryRestrictions.Builder());
        return new HodSavedQueryController(savedQueryService, documentsService, fieldTextParser, queryRestrictionsBuilderFactory);
    }
}
