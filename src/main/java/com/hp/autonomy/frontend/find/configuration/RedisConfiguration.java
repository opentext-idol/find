/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.HostAndPort;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.find.web.CacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.redis.RedisTokenRepository;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
@Conditional(RedisCondition.class)
@EnableRedisHttpSession
@EnableCaching
public class RedisConfiguration extends CachingConfigurerSupport{

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
    public TokenRepository tokenRepository() {
        final RedisConfig redisConfig = configService.getConfig().getRedis();
        final JedisConnectionFactory jedisConnectionFactory = redisConnectionFactory();
        final Integer database = redisConfig.getDatabase();

        final Pool<Jedis> pool;

        if(redisConfig.getSentinels().isEmpty()) {
            final HostAndPort address = redisConfig.getAddress();

            if (database != null) {
                pool = new JedisPool(jedisConnectionFactory.getPoolConfig(), address.getHost(), address.getPort(), Protocol.DEFAULT_TIMEOUT, null, database);
            }
            else {
                pool = new JedisPool(jedisConnectionFactory.getPoolConfig(), address.getHost(), address.getPort());
            }
        }
        else {
            final Set<String> sentinels = new HashSet<>();

            for(final HostAndPort hostAndPort : redisConfig.getSentinels()) {
                sentinels.add(hostAndPort.getHost() + ':' + hostAndPort.getPort());
            }

            if (database != null) {
                pool = new JedisSentinelPool(redisConfig.getMasterName(), sentinels, jedisConnectionFactory.getPoolConfig(), Protocol.DEFAULT_TIMEOUT, null, database);
            }
            else {
                pool = new JedisSentinelPool(redisConfig.getMasterName(), sentinels, jedisConnectionFactory.getPoolConfig());
            }
        }

        return new RedisTokenRepository(pool);
    }

    @Override
    @Bean
    public CacheManager cacheManager() {
        final RedisCacheManager cacheManager = new RedisCacheManager(cachingRedisTemplate());
        cacheManager.setUsePrefix(true);
        cacheManager.setCachePrefix(new DefaultRedisCachePrefix(":cache:" + dispatcherProperties.getProperty("application.version") + ':'));

        cacheManager.setDefaultExpiration(30 * 60);
        cacheManager.setExpires(CacheNames.CACHE_EXPIRES);

        return cacheManager;
    }

    @Override
    @Bean
    public CacheResolver cacheResolver() {
        final HodApplicationCacheResolver hodApplicationCacheResolver = new HodApplicationCacheResolver();
        hodApplicationCacheResolver.setCacheManager(cacheManager());

        return hodApplicationCacheResolver;
    }

    @Bean
    public RedisTemplate<Object, Object> cachingRedisTemplate() {
        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    // TODO if this works we need to librarify it
    public static class HodApplicationCacheResolver extends AbstractCacheResolver {
        static final char SEPARATOR = ':';

        @Override
        protected Collection<String> getCacheNames(final CacheOperationInvocationContext<?> context) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!(authentication instanceof HodAuthentication)) {
                throw new IllegalStateException("There is no HOD authentication token in the security context holder");
            }

            final HodAuthentication hodAuthentication = (HodAuthentication) authentication;
            final String applicationId = new ResourceIdentifier(hodAuthentication.getDomain(), hodAuthentication.getApplication()).toString();

            final Set<String> contextCacheNames = context.getOperation().getCacheNames();
            final Set<String> resolvedCacheNames = new HashSet<>();

            for (final String cacheName : contextCacheNames) {
                resolvedCacheNames.add(applicationId + SEPARATOR + cacheName);
            }

            return resolvedCacheNames;
        }
    }

}
