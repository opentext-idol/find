/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import java.util.Set;

public interface SavedSearchService<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> {

    Set<T> getAll();

    T get(long id);

    T create(T search);

    T update(T search);

    T getDashboardSearch(long id);

    void deleteById(long id);

}
