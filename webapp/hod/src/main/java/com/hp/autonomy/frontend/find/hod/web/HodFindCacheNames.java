/*
 * Copyright 2015-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.searchcomponents.hod.caching.HodCacheNames;

import java.util.Map;

public class HodFindCacheNames {

    /**
     * Caches' TTLs in seconds.
     */
    public static final Map<String, Long> CACHE_EXPIRES = new ImmutableMap.Builder<String, Long>()
            .putAll(FindCacheNames.CACHE_EXPIRES)
            .put(HodCacheNames.INDEX_FLAVOUR, 7L * 24L * 60L * 60L)
            .build();

}
