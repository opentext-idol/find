/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.filter.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldInfoConfigMixins;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdolFindConfigFileService extends FindConfigFileService<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {
    @Autowired
    public IdolFindConfigFileService(final FilterProvider filterProvider,
                                     final TextEncryptor textEncryptor,
                                     final JsonSerializer<FieldPath> fieldPathSerializer,
                                     final JsonDeserializer<FieldPath> fieldPathDeserializer) {
        super(filterProvider, textEncryptor, fieldPathSerializer, fieldPathDeserializer);
    }

    @Override
    public Class<IdolFindConfig> getConfigClass() {
        return IdolFindConfig.class;
    }

    @Override
    public IdolFindConfig getEmptyConfig() {
        return IdolFindConfig.builder().build();
    }

    @Override
    protected String getDefaultConfigFile() {
        return "/defaultIdolConfigFile.json";
    }

    @Override
    protected Map<Class<?>, Class<?>> customMixins() {
        return ImmutableMap.<Class<?>, Class<?>>builder()
                .put(Authentication.class, IdolAuthenticationMixins.class)
                .put(ServerConfig.class, ConfigurationFilterMixin.class)
                .put(ViewConfig.class, ConfigurationFilterMixin.class)
                .put(IdolFindConfig.class, ConfigurationFilterMixin.class)
                .put(FieldInfo.class, FieldInfoConfigMixins.class)
                .build();
    }
}
