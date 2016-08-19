/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.find.hod.configuration.HodAuthenticationMixins;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfigFileService;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class HodConfigFileConfiguration {
    @Bean
    public HodFindConfigFileService configService(final TextEncryptor textEncryptor, final FilterProvider filterProvider) {
        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .createXmlMapper(false)
                .mixIn(Authentication.class, HodAuthenticationMixins.class)
                .mixIn(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class)
                .mixIn(HodFindConfig.class, ConfigurationFilterMixin.class)
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .build();

        final HodFindConfigFileService configService = new HodFindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/defaultHodConfigFile.json");
        configService.setMapper(objectMapper);
        configService.setTextEncryptor(textEncryptor);
        configService.setFilterProvider(filterProvider);

        return configService;
    }
}
