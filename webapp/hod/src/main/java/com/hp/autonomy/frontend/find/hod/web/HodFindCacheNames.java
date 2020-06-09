/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
