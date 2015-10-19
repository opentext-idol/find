/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.HostAndPort;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.find.web.CacheNames;
import com.hp.autonomy.hod.redis.RedisTokenRepository;
import com.hp.autonomy.hod.redis.RedisTokenRepositoryConfig;
import com.hp.autonomy.hod.redis.RedisTokenRepositorySentinelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
@Conditional(RedisCondition.class)
@EnableRedisHttpSession
public class RedisConfiguration {

    @Autowired
    private ConfigService<FindConfig> configService;

    @Autowired
    private Properties dispatcherProperties;

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
        cacheManager.setCachePrefix(new DefaultRedisCachePrefix(":cache:" + dispatcherProperties.getProperty("application.version") + ':'));

        cacheManager.setDefaultExpiration(30 * 60);
        cacheManager.setExpires(CacheNames.CACHE_EXPIRES);

        return cacheManager;
    }

    @Bean
    public RedisTemplate<Object, Object> cachingRedisTemplate() {
        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

}
