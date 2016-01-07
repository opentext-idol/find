/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class CacheNames {

    public static final String DOCUMENTS = "documents";
    public static final String INDEXES = "indexes";
    public static final String PARAMETRIC_FIELDS = "parametric-fields";
    public static final String PARAMETRIC_VALUES = "parametric-values";
    public static final String PROMOTED_DOCUMENTS = "promoted-documents";
    public static final String RELATED_CONCEPTS = "related-concepts";
    public static final String SIMILAR_DOCUMENTS = "similar-documents";
    public static final String TYPE_AHEAD = "type-ahead";
    public static final String VISIBLE_INDEXES = "visible-indexes";

    /**
     * Caches' TTLs in seconds.
     */
    public static final Map<String, Long> CACHE_EXPIRES = new ImmutableMap.Builder<String, Long>()
        .put(DOCUMENTS, 60L * 5L)
        .put(PROMOTED_DOCUMENTS, 60L * 5L)
        .put(RELATED_CONCEPTS, 60L * 5L)
        .put(SIMILAR_DOCUMENTS, 60L * 5L)
        .put(TYPE_AHEAD, 24 * 60L)
        .build();

}
