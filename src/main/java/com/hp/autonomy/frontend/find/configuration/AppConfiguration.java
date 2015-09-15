/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.HostAndPort;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.configuration.SingleUserAuthenticationValidator;
import com.hp.autonomy.frontend.configuration.ValidationService;
import com.hp.autonomy.frontend.configuration.ValidationServiceImpl;
import com.hp.autonomy.frontend.configuration.Validator;
import com.hp.autonomy.frontend.view.hod.HodViewService;
import com.hp.autonomy.frontend.view.hod.HodViewServiceImpl;
import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentService;
import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.queryprofile.QueryProfileService;
import com.hp.autonomy.hod.client.api.queryprofile.QueryProfileServiceImpl;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.api.resource.ResourcesServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentService;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsService;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesService;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindRelatedConceptsService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindRelatedConceptsServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexServiceImpl;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.token.InMemoryTokenRepository;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.redis.RedisTokenRepository;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestServiceImpl;
import com.hp.autonomy.hod.sso.SpringSecurityTokenProxyService;
import com.hp.autonomy.hod.sso.UnboundTokenService;
import com.hp.autonomy.hod.sso.UnboundTokenServiceImpl;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableRedisHttpSession
public class AppConfiguration {

    @Autowired
    private FindConfigFileService configService;

    @Bean
    public IodConfigValidator iodConfigValidator() {
        return new IodConfigValidator();
    }

    @Bean
    public SingleUserAuthenticationValidator singleUserAuthenticationValidator() {
        final SingleUserAuthenticationValidator singleUserAuthenticationValidator = new SingleUserAuthenticationValidator();
        singleUserAuthenticationValidator.setConfigService(configService);

        return singleUserAuthenticationValidator;
    }

    @Bean
    public ValidationService<FindConfig> validationService() {
        final ValidationServiceImpl<FindConfig> validationService = new ValidationServiceImpl<>();

        // The type annotation here is required to make it compile
        //noinspection Convert2Diamond
        validationService.setValidators(new HashSet<Validator<?>>(Arrays.asList(
            singleUserAuthenticationValidator(),
            iodConfigValidator()
        )));

        // fix circular dependency
        configService.setValidationService(validationService);

        return validationService;
    }

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

    @Bean(name = "dispatcherObjectMapper")
    public ObjectMapper dispatcherObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);

        return mapper;
    }

    @Bean(name = "contextObjectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);
        mapper.addMixInAnnotations(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class);

        return mapper;
    }

    @Bean
    public HttpClient httpClient() {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final String proxyHost = System.getProperty("find.https.proxyHost");

        if(proxyHost != null) {
            final Integer proxyPort = Integer.valueOf(System.getProperty("find.https.proxyPort", "8080"));
            builder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        return builder.build();
    }

    @Bean
    public TokenRepository tokenRepository() {
        final TokenStoreConfig repoType = configService.getConfig().getTokenStoreConfig();

        if (repoType == TokenStoreConfig.REDIS) {
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

        } else {
            // TokenStoreConfig.INMEMORY is both a valid option and also our fallback option
            return new InMemoryTokenRepository();
        }
    }

    private HodServiceConfig.Builder hodServiceConfigBuilder() {
        return new HodServiceConfig.Builder(System.getProperty("find.iod.api", "https://api.idolondemand.com"))
            .setHttpClient(httpClient())
            .setTokenRepository(tokenRepository());
    }

    @Bean
    public HodServiceConfig initialHodServiceConfig() {
        return hodServiceConfigBuilder()
            .build();
    }

    @Bean
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl(initialHodServiceConfig());
    }

    @Bean
    public TokenProxyService tokenProxyService() {
        return new SpringSecurityTokenProxyService();
    }

    @Bean
    public HodServiceConfig hodServiceConfig() {
        return hodServiceConfigBuilder()
            .setTokenProxyService(tokenProxyService())
            .build();
    }

    @Bean
    public ResourcesService resourcesService() {
        return new ResourcesServiceImpl(hodServiceConfig());
    }

    @Bean
    public QueryTextIndexService<Documents> queryTextIndexService() {
        return QueryTextIndexServiceImpl.documentsService(hodServiceConfig());
    }

    @Bean
    public FindRelatedConceptsService relatedConceptsService() {
        return new FindRelatedConceptsServiceImpl(hodServiceConfig());
    }

    @Bean
    public RetrieveIndexFieldsService retrieveIndexFieldsService() {
        return new RetrieveIndexFieldsServiceImpl(hodServiceConfig());
    }

    @Bean
    public GetParametricValuesService getParametricValuesService() {
        return new GetParametricValuesServiceImpl(hodServiceConfig());
    }

    @Bean
    public GetContentService<Documents> getContentService() {
        return GetContentServiceImpl.documentsService(hodServiceConfig());
    }

    @Bean
    public ViewDocumentService viewDocumentService() {
        return new ViewDocumentServiceImpl(hodServiceConfig());
    }

    @Bean
    public QueryProfileService queryProfileService() {
        return new QueryProfileServiceImpl(hodServiceConfig());
    }

    @Bean
    public HodAuthenticationRequestService hodAuthenticationRequestService() {
        return new HodAuthenticationRequestServiceImpl(configService, authenticationService(), unboundTokenService());
    }

    @Bean
    public UnboundTokenService unboundTokenService() {
        return new UnboundTokenServiceImpl(authenticationService(), configService);
    }

    @Bean
    public HodViewService hodViewService() {
        return new HodViewServiceImpl(viewDocumentService(), getContentService(), queryTextIndexService());
    }

    @Bean
    public FindSimilarService<Documents> findSimilarService() {
        return FindSimilarServiceImpl.documentsService(hodServiceConfig());
    }
}
