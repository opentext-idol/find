/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.filter.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldInfoConfigMixins;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.config.FieldValueConfigMixins;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
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
            final JsonSerializer<FieldPath> fieldPathSerializer,
            final JsonDeserializer<FieldPath> fieldPathDeserializer) {
        super(filterProvider, textEncryptor, fieldPathSerializer, fieldPathDeserializer);
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
                .put(FieldValue.class, FieldValueConfigMixins.class)
                .build();
    }
}
