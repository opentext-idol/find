/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.Config;
import java.util.Collections;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService.CONFIG_FILE_LOCATION_HP;

public abstract class CustomizationConfigService<T extends Config<T>> extends BaseConfigFileService<T> {
    public static final String CONFIG_DIRECTORY = "customization";

    private final Class<T> configClass;
    private final T emptyConfig;

    protected CustomizationConfigService(
        final String configFileName,
        final String defaultFileName,
        final Class<T> configClass,
        final T emptyConfig,
        final Jackson2ObjectMapperBuilder objectMapperBuilder
    ) {
        this.configClass = configClass;
        this.emptyConfig = emptyConfig;

        final ObjectMapper objectMapper = objectMapperBuilder
            .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .createXmlMapper(false)
            .build();

        setMapper(objectMapper);
        setConfigFileLocation(FindConfigFileService.CONFIG_FILE_LOCATION);
        setDeprecatedConfigFileLocations(Collections.singletonList(CONFIG_FILE_LOCATION_HP));
        setConfigFileName(Paths.get(CONFIG_DIRECTORY).resolve(configFileName).toString());
        setDefaultConfigFile('/' + defaultFileName);
    }

    protected CustomizationConfigService(
        final String configFileName,
        final String defaultFileName,
        final Class<T> configClass,
        final T emptyConfig
    ) {
        this(configFileName, defaultFileName, configClass, emptyConfig, new Jackson2ObjectMapperBuilder());
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void init() throws Exception {
        Files.createDirectories(Paths.get(getConfigFileLocation()).getParent());
        super.init();
    }

    @Override
    public T getEmptyConfig() {
        return emptyConfig;
    }

    @Override
    public void postInitialise(final T config) {}

    @Override
    public Class<T> getConfigClass() {
        return configClass;
    }

    @Override
    public T generateDefaultLogin(final T config) {
        return config;
    }

    @Override
    public T withoutDefaultLogin(final T config) {
        return config;
    }

    @Override
    public T withHashedPasswords(final T config) {
        return config;
    }

    @Override
    public T preUpdate(final T config) {
        return config;
    }

    @Override
    public void postUpdate(final T config) {}
}
