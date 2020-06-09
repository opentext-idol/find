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

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.SingleUserAuthenticationValidator;
import com.hp.autonomy.frontend.find.hod.configuration.HodAuthenticationMixins;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
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
import com.hp.autonomy.hod.sso.UnboundTokenService;
import com.hp.autonomy.hod.sso.UnboundTokenServiceImpl;
import com.hp.autonomy.searchcomponents.hod.requests.HodQueryRestrictionsMixin;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.net.URL;

@Configuration
@EnableCaching
public class HodConfiguration {
    @Autowired
    private Environment environment;

    @Autowired
    private TokenRepository tokenRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Primary
    @Autowired
    public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder, final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever) {
        final ObjectMapper mapper = builder.createXmlMapper(false)
                .mixIn(Authentication.class, HodAuthenticationMixins.class)
                .mixIn(HodQueryRestrictions.class, HodQueryRestrictionsMixin.class)
                .build();

        mapper.setInjectableValues(new InjectableValues.Std().addValue(AuthenticationInformationRetriever.class, authenticationInformationRetriever));

        return mapper;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Bean
    public SingleUserAuthenticationValidator singleUserAuthenticationValidator(final ConfigService<? extends AuthenticationConfig<?>> configService) {
        final SingleUserAuthenticationValidator singleUserAuthenticationValidator = new SingleUserAuthenticationValidator();
        singleUserAuthenticationValidator.setConfigService(configService);

        return singleUserAuthenticationValidator;
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
    public HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple> hodServiceConfigBuilder(
            final HttpClient httpClient,
            final ObjectMapper objectMapper,
            final ConfigService<HodFindConfig> configService
    ) {
        final URL endpoint = configService.getConfig().getHod().getEndpointUrl();

        return new HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple>(endpoint.toString())
                .setHttpClient(httpClient)
                .setObjectMapper(objectMapper)
                .setTokenRepository(tokenRepository);
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
}
