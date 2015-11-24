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
    public IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser() {
        return new IdolResponseParser<>(new IdolResponseParser.Function<Error, AciErrorException>() {
            @Override
            public AciErrorException apply(final Error error) {
                final AciErrorException aciErrorException = new AciErrorException();
                aciErrorException.setErrorId(error.getErrorid());
                aciErrorException.setRawErrorId(error.getRawerrorid());
                aciErrorException.setErrorString(error.getErrorstring());
                aciErrorException.setErrorDescription(error.getErrordescription());
                aciErrorException.setErrorCode(error.getErrorcode());
                aciErrorException.setErrorTime(error.getErrortime());
                return aciErrorException;
            }
        }, new IdolResponseParser.BiFunction<String, Exception, ProcessorException>() {
            @Override
            public ProcessorException apply(final String message, final Exception cause) {
                return new ProcessorException(message, cause);
            }
        });
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
