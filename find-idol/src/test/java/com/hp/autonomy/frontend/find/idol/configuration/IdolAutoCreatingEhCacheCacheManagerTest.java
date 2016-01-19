/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.find.core.configuration.AbstractAutoCreatingEhCacheCacheManagerTest;
import net.sf.ehcache.config.CacheConfiguration;

public class IdolAutoCreatingEhCacheCacheManagerTest extends AbstractAutoCreatingEhCacheCacheManagerTest {
    @Override
    public void setUp() {
        autoCreatingEhCacheCacheManager = new IdolAutoCreatingEhCacheCacheManager(cacheManager, new CacheConfiguration());
    }
}
