/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractAutoCreatingEhCacheCacheManagerTest {
    @Mock
    protected CacheManager cacheManager;

    protected AutoCreatingEhCacheCacheManager autoCreatingEhCacheCacheManager;

    @Before
    public abstract void setUp();

    @Test
    public void getMissingCacheDefault() {
        final Ehcache ehcache = mock(Ehcache.class);
        when(ehcache.getStatus()).thenReturn(Status.STATUS_ALIVE);
        when(cacheManager.getEhcache(anyString())).thenReturn(ehcache);
        assertNotNull(autoCreatingEhCacheCacheManager.getMissingCache("SomeName"));
    }

    @Test
    public void getMissingCacheCustom() {
        final String name = FindCacheNames.DOCUMENTS;
        assertNotNull(autoCreatingEhCacheCacheManager.getMissingCache(name));
    }
}
