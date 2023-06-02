/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.searchcomponents.core.caching.CacheNames;

import java.util.Map;

public class FindCacheNames {

    public static final String DOCUMENTS = "documents";
    public static final String SIMILAR_DOCUMENTS = "similar-documents";

    /**
     * Caches' TTLs in seconds.
     */
    public static final Map<String, Long> CACHE_EXPIRES = new ImmutableMap.Builder<String, Long>()
        .put(DOCUMENTS, 60L * 5L)
        .put(CacheNames.RELATED_CONCEPTS, 60L * 5L)
        .put(SIMILAR_DOCUMENTS, 60L * 5L)
        .put(CacheNames.TYPE_AHEAD, 24 * 60L)
        .build();

}
