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

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.redis.RedisConfig;
import com.hp.autonomy.frontend.configuration.server.HostAndPort;
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
import java.util.stream.Collectors;

@Configuration
@Conditional(RedisCondition.class)
public class RedisHodConfiguration {

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Bean(destroyMethod = "destroy")
    public RedisTokenRepository tokenRepository() {
        final RedisConfig redisConfig = configService.getConfig().getRedis();
        final Integer database = redisConfig.getDatabase();
        final String password = redisConfig.getPassword();

        if (redisConfig.getSentinels().isEmpty()) {
            final HostAndPort address = redisConfig.getAddress();

            return new RedisTokenRepository(new RedisTokenRepositoryConfig.Builder()
                    .setHost(address.getHost())
                    .setPort(address.getPort())
                    .setPassword(password)
                    .setDatabase(database)
                    .build());
        } else {
            final Collection<RedisTokenRepositorySentinelConfig.HostAndPort> sentinels = redisConfig.getSentinels().stream().map(hostAndPort -> new RedisTokenRepositorySentinelConfig.HostAndPort(hostAndPort.getHost(), hostAndPort.getPort())).collect(Collectors.toCollection(HashSet::new));

            return new RedisTokenRepository(new RedisTokenRepositorySentinelConfig.Builder()
                    .setHostsAndPorts(sentinels)
                    .setMasterName(redisConfig.getMasterName())
                    .setDatabase(database)
                    .setPassword(password)
                    .build());
        }
    }

}
