/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.HostAndPort;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.find.core.beanconfiguration.RedisCondition;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
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
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@Conditional(RedisCondition.class)
@EnableRedisHttpSession
public class RedisConfiguration {

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Value("${application.commit}")
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

        cacheManager.setDefaultExpiration(30 * 60);
        cacheManager.setExpires(FindCacheNames.CACHE_EXPIRES);

        return cacheManager;
    }

    @Bean
    public RedisTemplate<Object, Object> cachingRedisTemplate() {
        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

}
