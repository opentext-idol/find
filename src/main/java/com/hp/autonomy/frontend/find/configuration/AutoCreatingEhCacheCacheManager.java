/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.frontend.find.web.CacheNames;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

public class AutoCreatingEhCacheCacheManager extends EhCacheCacheManager {

    private final CacheConfiguration defaults;

    public AutoCreatingEhCacheCacheManager(final net.sf.ehcache.CacheManager cacheManager, final CacheConfiguration defaults) {
        super(cacheManager);
        this.defaults = defaults;
    }

    @Override
    protected Cache getMissingCache(final String name) {
        final Cache missingCache = super.getMissingCache(name);

        if(missingCache == null) {
            final CacheConfiguration cacheConfiguration = defaults.clone()
                .name(name);

            final String cacheName = HodApplicationCacheResolver.getOriginalName(name);

            if(CacheNames.CACHE_EXPIRES.containsKey(cacheName)) {
                cacheConfiguration.setTimeToLiveSeconds(CacheNames.CACHE_EXPIRES.get(cacheName));
            }

            final net.sf.ehcache.Cache ehcache = new net.sf.ehcache.Cache(cacheConfiguration);
            ehcache.initialise();

            return new EhCacheCache(ehcache);
        }
        else {
            return missingCache;
        }
    }
}
