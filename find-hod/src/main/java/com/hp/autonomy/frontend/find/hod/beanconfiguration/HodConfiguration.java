/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.SingleUserAuthenticationValidator;
import com.hp.autonomy.frontend.find.hod.configuration.HodAuthenticationMixins;
import com.hp.autonomy.hod.caching.HodApplicationCacheResolver;
import com.hp.autonomy.hod.client.api.analysis.autocomplete.AutocompleteService;
import com.hp.autonomy.hod.client.api.analysis.autocomplete.AutocompleteServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersService;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersServiceImpl;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestServiceImpl;
import com.hp.autonomy.hod.sso.HodSsoConfig;
import com.hp.autonomy.hod.sso.SpringSecurityTokenProxyService;
import com.hp.autonomy.hod.sso.UnboundTokenService;
import com.hp.autonomy.hod.sso.UnboundTokenServiceImpl;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@EnableCaching
public class HodConfiguration extends CachingConfigurerSupport {
    public static final String SSO_PAGE_PROPERTY = "${find.hod.sso:https://www.idolondemand.com/sso.html}";
    public static final String HOD_API_URL_PROPERTY = "${find.iod.api:https://api.havenondemand.com}";
    public static final String SIMPLE_CACHE_RESOLVER_NAME = "simpleCacheResolver";

    @Autowired
    private Environment environment;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CacheManager cacheManager;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Primary
    @Autowired
    public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false)
                .mixIn(Authentication.class, HodAuthenticationMixins.class)
                .build();
    }

    @Bean
    public SingleUserAuthenticationValidator singleUserAuthenticationValidator(final ConfigService<? extends AuthenticationConfig<?>> configService) {
        final SingleUserAuthenticationValidator singleUserAuthenticationValidator = new SingleUserAuthenticationValidator();
        singleUserAuthenticationValidator.setConfigService(configService);

        return singleUserAuthenticationValidator;
    }

    @Override
    @Bean
    public CacheResolver cacheResolver() {
        final HodApplicationCacheResolver hodApplicationCacheResolver = new HodApplicationCacheResolver();
        hodApplicationCacheResolver.setCacheManager(cacheManager);

        return hodApplicationCacheResolver;
    }

    // Resolver for caches which are not application-specific
    @Bean(name = SIMPLE_CACHE_RESOLVER_NAME)
    public CacheResolver simpleCacheResolver() {
        final SimpleCacheResolver resolver = new SimpleCacheResolver();
        resolver.setCacheManager(cacheManager);
        return resolver;
    }

    @Bean
    public HttpClient httpClient() {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final String proxyHost = environment.getProperty("find.https.proxyHost");

        if (proxyHost != null) {
            final Integer proxyPort = Integer.valueOf(environment.getProperty("find.https.proxyPort", "8080"));
            builder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        builder.disableCookieManagement();

        return builder.build();
    }

    @Bean
    public HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple> hodServiceConfigBuilder(final HttpClient httpClient, @Qualifier("hodSearchResultObjectMapper") final ObjectMapper hodSearchResultObjectMapper) {
        final String endpoint = environment.getProperty("find.iod.api", "https://api.havenondemand.com");

        return new HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple>(endpoint)
                .setHttpClient(httpClient)
                .setObjectMapper(hodSearchResultObjectMapper)
                .setTokenRepository(tokenRepository);
    }

    @Bean
    public TokenProxyService<EntityType.Combined, TokenType.Simple> tokenProxyService() {
        return new SpringSecurityTokenProxyService();
    }

    @Bean
    public HodServiceConfig<EntityType.Combined, TokenType.Simple> hodServiceConfig(
            final HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple> hodServiceConfigBuilder,
            final TokenProxyService<EntityType.Combined, TokenType.Simple> tokenProxyService) {
        return hodServiceConfigBuilder
                .setTokenProxyService(tokenProxyService)
                .build();
    }

    @Bean
    public AuthenticationService authenticationService(final HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple> hodServiceConfigBuilder) {
        return new AuthenticationServiceImpl(hodServiceConfigBuilder.build());
    }

    @Bean
    public HodAuthenticationRequestService hodAuthenticationRequestService(final ConfigService<? extends HodSsoConfig> configService, final AuthenticationService authenticationService, final UnboundTokenService<TokenType.HmacSha1> unboundTokenService) {
        return new HodAuthenticationRequestServiceImpl(configService, authenticationService, unboundTokenService);
    }

    @Bean
    public UnboundTokenService<TokenType.HmacSha1> unboundTokenService(final ConfigService<? extends HodSsoConfig> configService, final AuthenticationService authenticationService) throws HodErrorException {
        return new UnboundTokenServiceImpl(authenticationService, configService);
    }

    @Bean
    public UserStoreUsersService userStoreUsersService(final HodServiceConfig<EntityType.Combined, TokenType.Simple> hodServiceConfig) {
        return new UserStoreUsersServiceImpl(hodServiceConfig);
    }

    @Bean
    public AutocompleteService autocompleteService(final HodServiceConfig<EntityType.Combined, TokenType.Simple> hodServiceConfig) {
        return new AutocompleteServiceImpl(hodServiceConfig);
    }
}
