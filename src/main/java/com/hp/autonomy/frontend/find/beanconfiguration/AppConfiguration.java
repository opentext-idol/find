/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.beanconfiguration;


import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.filter.ConfigEnvironmentVariableFilter;
import com.hp.autonomy.frontend.find.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.configuration.TextEncryptorPasswordFactory;
import com.hp.autonomy.frontend.logging.ApplicationStartLogger;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.Set;

/**
 * Contains beans useful in all configurations
 */
@Configuration
@ComponentScan(
    basePackages = "com.hp.autonomy.frontend.find",
    excludeFilters = {
        @ComponentScan.Filter(Controller.class),
        @ComponentScan.Filter(RestController.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = DispatcherServletConfiguration.class)
    }
)
public class AppConfiguration {

    @Autowired
    private ConfigService<HodFindConfig> configService;

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

    //TODO: merge properties files
    @Bean
    public PropertiesFactoryBean dispatcherProperties() {
        return getPropertiesFactoryBean(new ClassPathResource("com/hp/autonomy/frontend/find/dispatcher.properties"));
    }

    @Bean
    public PropertiesFactoryBean applicationProperties() {
        return getPropertiesFactoryBean(new ClassPathResource("com/hp/autonomy/frontend/find/find.properties"));
    }

    private PropertiesFactoryBean getPropertiesFactoryBean(final ClassPathResource location) {
        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(location);

        return bean;
    }

    @Bean
    public ApplicationStartLogger applicationStartLogger() {
        return new ApplicationStartLogger();
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.ENGLISH);

        return sessionLocaleResolver;
    }

    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setBasename("com.hp.autonomy.frontend.find.i18n");

        return resourceBundleMessageSource;
    }

    @Bean
    public ConfigEnvironmentVariableFilter configEnvironmentVariableFilter() {
        final ConfigEnvironmentVariableFilter configEnvironmentVariableFilter = new ConfigEnvironmentVariableFilter();
        configEnvironmentVariableFilter.setConfigPage("/configError");
        configEnvironmentVariableFilter.setConfigService(configService);

        return configEnvironmentVariableFilter;
    }

}
