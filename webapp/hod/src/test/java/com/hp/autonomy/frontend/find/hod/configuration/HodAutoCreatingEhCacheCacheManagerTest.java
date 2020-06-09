/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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
