/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.frontend.find.core.configuration.AutoCreatingEhCacheCacheManager;
import com.hp.autonomy.hod.caching.HodCacheNameResolver;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(InMemoryCondition.class)
public class HodAutoCreatingEhCacheCacheManager extends AutoCreatingEhCacheCacheManager {
    private final HodCacheNameResolver cacheNameResolver;

    @Autowired
    public HodAutoCreatingEhCacheCacheManager(final CacheManager cacheManager, final CacheConfiguration defaults, final HodCacheNameResolver cacheNameResolver) {
        super(cacheManager, defaults);
        this.cacheNameResolver = cacheNameResolver;
    }

    @Override
    protected String getCacheName(final String name) {
        return cacheNameResolver.getOriginalName(name);
    }
}
