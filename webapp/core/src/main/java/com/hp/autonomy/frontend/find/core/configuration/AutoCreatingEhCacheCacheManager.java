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

package com.hp.autonomy.frontend.find.core.configuration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Conditional;

import java.util.Map;

@Conditional(InMemoryCondition.class)
public abstract class AutoCreatingEhCacheCacheManager extends EhCacheCacheManager {
    private final Map<String, Long> cacheExpires;
    private final CacheConfiguration defaults;

    protected AutoCreatingEhCacheCacheManager(final CacheManager cacheManager, final Map<String, Long> cacheExpires, final CacheConfiguration defaults) {
        super(cacheManager);
        this.defaults = defaults;
        this.cacheExpires = ImmutableMap.copyOf(cacheExpires);
    }

    @Override
    protected Cache getMissingCache(final String name) {
        final Cache missingCache = super.getMissingCache(name);

        if (missingCache == null) {
            final CacheConfiguration cacheConfiguration = defaults.clone().name(name);

            final String cacheName = getCacheName(name);

            if (cacheExpires.containsKey(cacheName)) {
                cacheConfiguration.setTimeToLiveSeconds(cacheExpires.get(cacheName));
            }

            final net.sf.ehcache.Cache ehcache = new net.sf.ehcache.Cache(cacheConfiguration);
            ehcache.initialise();

            return new EhCacheCache(ehcache);
        } else {
            return missingCache;
        }
    }

    protected abstract String getCacheName(final String name);
}
