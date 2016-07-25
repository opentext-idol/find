/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchTest;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;

public class SavedQueryTest extends AbstractSavedSearchTest<SavedQuery> {
    @Override
    protected SavedSearch.Builder<SavedQuery> createBuilder() {
        return new SavedQuery.Builder();
    }
}
