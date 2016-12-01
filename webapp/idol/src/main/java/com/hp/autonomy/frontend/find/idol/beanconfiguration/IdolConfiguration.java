/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.aci.AbstractConfigurableAciService;
import com.hp.autonomy.frontend.configuration.aci.CommunityService;
import com.hp.autonomy.frontend.configuration.aci.CommunityServiceImpl;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthenticationValidator;
import com.hp.autonomy.frontend.configuration.filter.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.server.ServerConfigValidator;
import com.hp.autonomy.frontend.find.idol.configuration.IdolAuthenticationMixins;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfigFileService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldInfoConfigMixins;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.requests.IdolQueryRestrictionsMixin;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.user.UserService;
import com.hp.autonomy.user.UserServiceImpl;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.http.client.HttpClient;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@ImportResource("required-statistics.xml")
public class IdolConfiguration {

    @Autowired
    private TextEncryptor textEncryptor;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Autowired
    @Primary
    public ObjectMapper jacksonObjectMapper(
            final Jackson2ObjectMapperBuilder builder,
            final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        final ObjectMapper mapper = builder
                .createXmlMapper(false)
                .mixIn(Authentication.class, IdolAuthenticationMixins.class)
                .mixIn(QueryRestrictions.class, IdolQueryRestrictionsMixin.class)
                .mixIn(IdolQueryRestrictions.class, IdolQueryRestrictionsMixin.class)
                .build();

        mapper.setInjectableValues(new InjectableValues.Std().addValue(AuthenticationInformationRetriever.class, authenticationInformationRetriever));

        return mapper;
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Autowired
    public IdolFindConfigFileService configFileService(final Jackson2ObjectMapperBuilder builder, final FilterProvider filterProvider) {
        final ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.addMixIn(Authentication.class, IdolAuthenticationMixins.class);
        objectMapper.addMixIn(ServerConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(ViewConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(IdolFindConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(FieldInfo.class, FieldInfoConfigMixins.class);

        final IdolFindConfigFileService configService = new IdolFindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/defaultIdolConfigFile.json");
        configService.setMapper(objectMapper);
        configService.setTextEncryptor(textEncryptor);
        configService.setFilterProvider(filterProvider);

        return configService;
    }

    @Bean
    public UserService userService(final ConfigService<IdolFindConfig> configService, final AciService aciService, final ProcessorFactory processorFactory) {
        return new UserServiceImpl(configService, aciService, processorFactory);
    }

    @Bean
    @Autowired
    public CommunityService communityService(final AciService aciService, final ProcessorFactory processorFactory) {
        final CommunityServiceImpl communityService = new CommunityServiceImpl();
        communityService.setAciService(aciService);
        communityService.setProcessorFactory(processorFactory);

        return communityService;
    }

    @Bean
    public AciService statsServerAciService(@Qualifier("postingAciService") final AciService aciService, final ConfigService<IdolFindConfig> configService) {
        return new AbstractConfigurableAciService(aciService) {
            @Override
            public AciServerDetails getServerDetails() {
                return configService.getConfig().getStatsServer().getServer().toAciServerDetails();
            }
        };
    }

    @Bean
    public CommunityAuthenticationValidator communityAuthenticationValidator(
            final AciService validatorAciService,
            final ProcessorFactory processorFactory
    ) {
        final CommunityAuthenticationValidator communityAuthenticationValidator = new CommunityAuthenticationValidator();

        communityAuthenticationValidator.setAciService(validatorAciService);
        communityAuthenticationValidator.setProcessorFactory(processorFactory);

        return communityAuthenticationValidator;
    }

    @Bean
    public ServerConfigValidator serverConfigValidator(
            final AciService validatorAciService,
            final ProcessorFactory processorFactory
    ) {
        final ServerConfigValidator serverConfigValidator = new ServerConfigValidator();

        serverConfigValidator.setAciService(validatorAciService);
        serverConfigValidator.setProcessorFactory(processorFactory);

        return serverConfigValidator;
    }

    @Bean
    public AciService postingAciService(final HttpClient httpClient) {
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(httpClient);
        aciHttpClient.setUsePostMethod(true);

        return new AciServiceImpl(aciHttpClient);
    }
}
