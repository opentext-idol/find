/*
 * Copyright 2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import org.jasypt.util.text.TextEncryptor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.FilterProvider;

import java.util.Collections;
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
                                    final ValueSerializer<FieldPath> fieldPathSerializer,
                                    final ValueDeserializer<FieldPath> fieldPathDeserializer) {

        final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .addMixIns(customMixins())
            .addModule(new SimpleModule()
                    .addSerializer(FieldPath.class, fieldPathSerializer)
                    .addDeserializer(FieldPath.class, fieldPathDeserializer))
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
