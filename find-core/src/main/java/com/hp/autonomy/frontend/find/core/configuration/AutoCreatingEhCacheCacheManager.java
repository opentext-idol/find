/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Conditional;

@Conditional(InMemoryCondition.class)
public abstract class AutoCreatingEhCacheCacheManager extends EhCacheCacheManager {

    private final CacheConfiguration defaults;

    protected AutoCreatingEhCacheCacheManager(final CacheManager cacheManager, final CacheConfiguration defaults) {
        super(cacheManager);
        this.defaults = defaults;
    }

    @Override
    protected Cache getMissingCache(final String name) {
        final Cache missingCache = super.getMissingCache(name);

        if (missingCache == null) {
            final CacheConfiguration cacheConfiguration = defaults.clone().name(name);

            final String cacheName = getCacheName(name);

            if (FindCacheNames.CACHE_EXPIRES.containsKey(cacheName)) {
                cacheConfiguration.setTimeToLiveSeconds(FindCacheNames.CACHE_EXPIRES.get(cacheName));
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
