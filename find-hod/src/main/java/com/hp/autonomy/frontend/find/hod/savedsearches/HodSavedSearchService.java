/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches;

import com.hp.autonomy.frontend.find.core.savedsearches.NameAndDomain;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HodSavedSearchService extends SavedSearchServiceImpl {
    public HodSavedSearchService() {
        super(new NameAndDomain("PUBLIC_INDEXES", "wiki_eng"));
    }
}
