/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.redis.RedisConfig;
import com.hp.autonomy.frontend.configuration.server.HostAndPort;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.RedisCondition;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.web.HodFindCacheNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.ConfigureNotifyKeyspaceEventsAction;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@Conditional(RedisCondition.class)
@EnableRedisHttpSession
public class RedisConfiguration {
    private static final int DEFAULT_EXPIRATION = 30 * 60;

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String commit;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        final RedisConfig config = configService.getConfig().getRedis();
        final JedisConnectionFactory connectionFactory;

        //If we haven't specified any sentinels then assume non-distributed setup
        if (config.getSentinels().isEmpty()) {
            connectionFactory = new JedisConnectionFactory();
            connectionFactory.setHostName(config.getAddress().getHost());
            connectionFactory.setPort(config.getAddress().getPort());
        } else {
            final RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(config.getMasterName());
            for (final HostAndPort node : config.getSentinels()) {
                sentinelConfig.sentinel(node.getHost(), node.getPort());
            }

            connectionFactory = new JedisConnectionFactory(sentinelConfig);
        }

        final Integer database = config.getDatabase();

        if (database != null) {
            connectionFactory.setDatabase(database);
        }

        connectionFactory.setPassword(config.getPassword());

        return connectionFactory;
    }

    @Bean
    public CacheManager cacheManager() {
        final RedisCacheManager cacheManager = new RedisCacheManager(cachingRedisTemplate());
        cacheManager.setUsePrefix(true);
        cacheManager.setCachePrefix(new DefaultRedisCachePrefix(":cache:" + commit + ':'));

        cacheManager.setDefaultExpiration(DEFAULT_EXPIRATION);
        cacheManager.setExpires(HodFindCacheNames.CACHE_EXPIRES);

        return cacheManager;
    }

    @Bean
    public RedisTemplate<Object, Object> cachingRedisTemplate() {
        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    @Bean
    public ConfigureRedisAction configureRedisAction() {
        // The config action might not be available in a secure redis (eg: Azure)
        return configService.getConfig().getRedis().getAutoConfigure() ? new ConfigureNotifyKeyspaceEventsAction() : ConfigureRedisAction.NO_OP;
    }

}
