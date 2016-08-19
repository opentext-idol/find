/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(InMemoryCondition.class)
public class InMemoryConfiguration {
    @Bean
    public CacheConfiguration defaultCacheConfiguration() {
        return new CacheConfiguration()
                .eternal(false)
                .maxElementsInMemory(1000)
                .overflowToDisk(false)
                .diskPersistent(false)
                .timeToIdleSeconds(0)
                .timeToLiveSeconds(30 * 60)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU);
    }

    @Bean(destroyMethod = "shutdown")
    public CacheManager cacheManager() {
        final net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration()
                .defaultCache(defaultCacheConfiguration())
                .updateCheck(false);

        return new CacheManager(configuration);
    }
}
