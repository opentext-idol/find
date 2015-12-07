/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.find.hod.configuration.HodAuthenticationMixins;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfigFileService;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HodConfigFileConfiguration {
    @Autowired
    private TextEncryptor textEncryptor;

    @Autowired
    private FilterProvider filterProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public HodFindConfigFileService configService() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.addMixIn(Authentication.class, HodAuthenticationMixins.class);

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
