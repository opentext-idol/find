/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;

public class HodSavedSearchQueryControllerTest extends SavedQueryControllerTest<ResourceIdentifier, HodSearchResult, HodErrorException> {
    @Override
    protected SavedQueryController<ResourceIdentifier, HodSearchResult, HodErrorException> constructController() {
        return new HodSavedQueryController(savedQueryService, documentsService, queryRestrictionsBuilder);
    }
}
