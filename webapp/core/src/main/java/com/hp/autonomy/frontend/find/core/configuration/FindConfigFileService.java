/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import java.util.Collections;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Map;

/**
 * Common logic applied to HoD and Idol config implementations
 */
public abstract class FindConfigFileService<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> extends AbstractAuthenticatingConfigFileService<C> {
    public static final String CONFIG_FILE_LOCATION = "idol.find.home";
    public static final String CONFIG_FILE_LOCATION_HP = "hp.find.home";
    public static final String CONFIG_FILE_LOCATION_SPEL = "${"+CONFIG_FILE_LOCATION+":${"+CONFIG_FILE_LOCATION_HP+":}}";
    private static final String CONFIG_FILE_NAME = "config.json";

    protected FindConfigFileService(final FilterProvider filterProvider,
                                    final TextEncryptor textEncryptor,
                                    final JsonSerializer<FieldPath> fieldPathSerializer,
                                    final JsonDeserializer<FieldPath> fieldPathDeserializer) {

        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
            .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .mixIns(customMixins())
            .serializersByType(ImmutableMap.of(FieldPath.class, fieldPathSerializer))
            .deserializersByType(ImmutableMap.of(FieldPath.class, fieldPathDeserializer))
            .createXmlMapper(false)
            .build();

        setConfigFileLocation(CONFIG_FILE_LOCATION);
        setDeprecatedConfigFileLocations(Collections.singletonList(CONFIG_FILE_LOCATION_HP));
        setConfigFileName(CONFIG_FILE_NAME);
        setDefaultConfigFile(getDefaultConfigFile());
        setMapper(objectMapper);
        setTextEncryptor(textEncryptor);
        setFilterProvider(filterProvider);
    }

    protected abstract String getDefaultConfigFile();

    protected abstract Map<Class<?>, Class<?>> customMixins();

    @Override
    public void postInitialise(final C config) {
        postUpdate(config);
    }

    @Override
    public C preUpdate(final C config) {
        return config;
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void postUpdate(final C config) {}
}
