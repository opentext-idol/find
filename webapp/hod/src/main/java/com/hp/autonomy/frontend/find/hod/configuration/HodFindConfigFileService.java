/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.filter.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldInfoConfigMixins;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class HodFindConfigFileService extends FindConfigFileService<HodFindConfig, HodFindConfig.HodFindConfigBuilder> {
    @Autowired
    public HodFindConfigFileService(
            final FilterProvider filterProvider,
            final TextEncryptor textEncryptor,
            final JsonDeserializer<TagName> tagNameDeserializer) {
        super(filterProvider, textEncryptor, tagNameDeserializer);
    }

    @Override
    public Class<HodFindConfig> getConfigClass() {
        return HodFindConfig.class;
    }

    @Override
    public HodFindConfig getEmptyConfig() {
        return HodFindConfig.builder().build();
    }

    @Override
    protected String getDefaultConfigFile() {
        return "/defaultHodConfigFile.json";
    }

    @Override
    protected Map<Class<?>, Class<?>> customMixins() {
        return ImmutableMap.<Class<?>, Class<?>>builder()
                .put(Authentication.class, HodAuthenticationMixins.class)
                .put(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class)
                .put(HodFindConfig.class, ConfigurationFilterMixin.class)
                .put(FieldInfo.class, FieldInfoConfigMixins.class)
                .build();
    }
}
