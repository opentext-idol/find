/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;

public class IdolSavedSearchQueryControllerTest extends SavedQueryControllerTest<String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> {
    @Override
    protected SavedQueryController<String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> constructController() {
        return new IdolSavedQueryController(savedQueryService, documentsService, new IdolQueryRestrictions.Builder());
    }
}
