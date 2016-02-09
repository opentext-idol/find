/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.searchcomponents.core.caching.CacheNames;

import java.util.Map;

public class FindCacheNames {

    public static final String DOCUMENTS = "documents";
    public static final String PROMOTED_DOCUMENTS = "promoted-documents";
    public static final String SIMILAR_DOCUMENTS = "similar-documents";
    public static final String TYPE_AHEAD = "type-ahead";

    /**
     * Caches' TTLs in seconds.
     */
    public static final Map<String, Long> CACHE_EXPIRES = new ImmutableMap.Builder<String, Long>()
            .put(DOCUMENTS, 60L * 5L)
            .put(PROMOTED_DOCUMENTS, 60L * 5L)
            .put(CacheNames.RELATED_CONCEPTS, 60L * 5L)
            .put(SIMILAR_DOCUMENTS, 60L * 5L)
            .put(TYPE_AHEAD, 24 * 60L)
            .build();

}
