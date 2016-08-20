/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.frontend.find.core.configuration.AutoCreatingEhCacheCacheManager;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(InMemoryCondition.class)
public class IdolAutoCreatingEhCacheCacheManager extends AutoCreatingEhCacheCacheManager {
    @Autowired
    public IdolAutoCreatingEhCacheCacheManager(final CacheManager cacheManager, final CacheConfiguration defaults) {
        super(cacheManager, defaults);
    }

    @Override
    protected String getCacheName(final String name) {
        return name;
    }
}
