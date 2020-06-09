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
