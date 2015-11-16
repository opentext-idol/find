/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactoryImpl;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.hp.autonomy.frontend.find.idol.configuration.CommunityAciService;
import com.hp.autonomy.frontend.find.idol.configuration.ContentAciService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfigFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.user.UserService;
import com.hp.autonomy.user.UserServiceImpl;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(IdolCondition.class)
public class IdolConfiguration {

    @Autowired
    private TextEncryptor textEncryptor;

    @Autowired
    private FilterProvider filterProvider;

    @Bean
    public ConfigService<IdolFindConfig> configFileService() {
        final IdolFindConfigFileService configService = new IdolFindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/com/hp/autonomy/frontend/find/configuration/defaultIdolConfigFile.json");
        configService.setMapper(objectMapper());
        configService.setTextEncryptor(textEncryptor);
        configService.setFilterProvider(filterProvider);

        return configService;
    }

    @Bean(name = "contextObjectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // TODO add mixins

        return mapper;
    }

    @Bean
    public HttpClient httpClient() {
        final SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(90000)
            .build();

        return HttpClientBuilder.create()
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(120)
            .setDefaultSocketConfig(socketConfig)
            .build();
    }

    @Bean
    public HttpClient testHttpClient() {
        final SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(2000)
            .build();

        return HttpClientBuilder.create()
            .setMaxConnPerRoute(5)
            .setMaxConnTotal(5)
            .setDefaultSocketConfig(socketConfig)
            .build();
    }

    @Bean
    public IdolAnnotationsProcessorFactory processorFactory() {
        return new IdolAnnotationsProcessorFactoryImpl();
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl(configFileService(), aciService(), processorFactory());
    }

    @Bean
    public AciService contentAciService() {
        return new ContentAciService(aciService(), configFileService());
    }

    @Bean
    public AciService communutyAciService() {
        return new CommunityAciService(aciService(), configFileService());
    }

    @Bean
    public AciService aciService() {
        return new AciServiceImpl(new AciHttpClientImpl(httpClient()));
    }

    @Bean
    public AciService testAciService() {
        return new AciServiceImpl(new AciHttpClientImpl(testHttpClient()));
    }

}
