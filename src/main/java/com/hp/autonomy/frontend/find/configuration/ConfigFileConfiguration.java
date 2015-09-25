/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ConfigFileConfiguration {

    @Bean
    public TextEncryptor textEncryptor() {
        final TextEncryptorPasswordFactory passwordFactory = new TextEncryptorPasswordFactory();

        final BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();

        try {
            basicTextEncryptor.setPassword(passwordFactory.getObject());
        } catch (final Exception e) {
            throw new BeanInitializationException("Failed to initialize TextEncryptor for some reason", e);
        }

        return basicTextEncryptor;
    }

    @Bean
    public SimpleFilterProvider filterProvider() {
        final Set<String> set = ImmutableSet.of(
            "productType",
            "indexErrorMessage",
            "enabled",
            "plaintextPassword",
            "currentPassword"
        );

        final SimpleBeanPropertyFilter.SerializeExceptFilter filter = new SimpleBeanPropertyFilter.SerializeExceptFilter(set);

        return new SimpleFilterProvider(ImmutableMap.of("configurationFilter", filter));
    }

    @Bean
    public FindConfigFileService configService() {
        final FindConfigFileService configService = new FindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/com/hp/autonomy/frontend/find/configuration/defaultConfigFile.json");
        configService.setMapper(objectMapper());
        configService.setTextEncryptor(textEncryptor());
        configService.setFilterProvider(filterProvider());

        return configService;
    }

    @Bean(name = "contextObjectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);
        mapper.addMixInAnnotations(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class);

        return mapper;
    }
}
