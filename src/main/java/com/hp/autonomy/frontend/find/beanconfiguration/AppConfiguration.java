/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.beanconfiguration;


import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.find.configuration.TextEncryptorPasswordFactory;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Contains beans useful in all configurations
 */
@Configuration
public class AppConfiguration {

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
}
