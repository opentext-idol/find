/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;


import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchRepository;

/**
 * Repository containing actions which can be performed on the main searches table
 * Spring automatically implements basic operations
 */
public interface SavedQueryRepository extends SavedSearchRepository<SavedQuery> {
}
