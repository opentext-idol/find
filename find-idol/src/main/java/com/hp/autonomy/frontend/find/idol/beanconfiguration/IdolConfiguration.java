/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactoryImpl;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfigFileService;
import com.hp.autonomy.frontend.view.idol.IdolViewServerService;
import com.hp.autonomy.frontend.view.idol.ViewServerService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.Error;
import com.hp.autonomy.types.idol.IdolResponseParser;
import com.hp.autonomy.user.UserService;
import com.hp.autonomy.user.UserServiceImpl;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdolConfiguration {
    private static final int HTTP_SOCKET_TIMEOUT = 90000;
    private static final int HTTP_MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int HTTP_MAX_CONNECTIONS_TOTAL = 120;

    @Autowired
    private TextEncryptor textEncryptor;

    @Autowired
    private FilterProvider filterProvider;

    @Bean
    public IdolFindConfigFileService configFileService() {
        final IdolFindConfigFileService configService = new IdolFindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/defaultIdolConfigFile.json");
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
    public AciResponseJaxbProcessorFactory aciResponseProcessorFactory() {
        return new AciResponseJaxbProcessorFactory();
    }

    @Bean
    @Autowired
    public ViewServerService viewServerService(final AciService contentAciService, final AciService viewAciService, final ConfigService<IdolFindConfig> configService) {
        return new IdolViewServerService(contentAciService, viewAciService, aciResponseProcessorFactory(), configService);
    }

    @Bean
    public HttpClient httpClient() {
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(HTTP_SOCKET_TIMEOUT)
                .build();

        return HttpClientBuilder.create()
                .setMaxConnPerRoute(HTTP_MAX_CONNECTIONS_PER_ROUTE)
                .setMaxConnTotal(HTTP_MAX_CONNECTIONS_TOTAL)
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
    public AciService aciService() {
        return new AciServiceImpl(new AciHttpClientImpl(httpClient()));
    }
}
