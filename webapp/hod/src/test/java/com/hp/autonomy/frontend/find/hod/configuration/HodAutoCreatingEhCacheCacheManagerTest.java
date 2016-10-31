/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.find.core.configuration.AbstractAutoCreatingEhCacheCacheManagerTest;
import com.hp.autonomy.hod.caching.HodCacheNameResolver;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

public class HodAutoCreatingEhCacheCacheManagerTest extends AbstractAutoCreatingEhCacheCacheManagerTest<HodAutoCreatingEhCacheCacheManager> {
    @Mock
    private HodCacheNameResolver cacheNameResolver;

    @Override
    public void setUp() {
        autoCreatingEhCacheCacheManager = new HodAutoCreatingEhCacheCacheManager(cacheManager, new CacheConfiguration(), cacheNameResolver);
    }

    @Test
    public void getCacheName() {
        autoCreatingEhCacheCacheManager.getCacheName("SomeName");
        verify(cacheNameResolver).getOriginalName("SomeName");
    }
}
