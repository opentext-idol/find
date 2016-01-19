/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchServiceImpl;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.springframework.stereotype.Service;

@Service
public class HodSavedSearchService extends SavedSearchServiceImpl<ResourceIdentifier> {
    public HodSavedSearchService() {
        super(ResourceIdentifier.WIKI_ENG);
    }
}
