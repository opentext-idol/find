/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.HostAndPort;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.find.core.beanconfiguration.RedisCondition;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.redis.RedisTokenRepository;
import com.hp.autonomy.hod.redis.RedisTokenRepositoryConfig;
import com.hp.autonomy.hod.redis.RedisTokenRepositorySentinelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;

@Configuration
@Conditional(RedisCondition.class)
public class RedisHodConfiguration {

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Bean(destroyMethod = "destroy")
    public RedisTokenRepository tokenRepository() {
        final RedisConfig redisConfig = configService.getConfig().getRedis();
        final Integer database = redisConfig.getDatabase();

        if (redisConfig.getSentinels().isEmpty()) {
            final HostAndPort address = redisConfig.getAddress();

            return new RedisTokenRepository(new RedisTokenRepositoryConfig.Builder()
                    .setHost(address.getHost())
                    .setPort(address.getPort())
                    .setDatabase(database)
                    .build());
        } else {
            final Collection<RedisTokenRepositorySentinelConfig.HostAndPort> sentinels = new HashSet<>();

            for (final HostAndPort hostAndPort : redisConfig.getSentinels()) {
                sentinels.add(new RedisTokenRepositorySentinelConfig.HostAndPort(hostAndPort.getHost(), hostAndPort.getPort()));
            }

            return new RedisTokenRepository(new RedisTokenRepositorySentinelConfig.Builder()
                    .setHostsAndPorts(sentinels)
                    .setMasterName(redisConfig.getMasterName())
                    .setDatabase(database)
                    .build());
        }
    }

}
