/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.filter.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldInfoConfigMixins;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.config.FieldValueConfigMixins;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import db.migration.h2.V11_4_0_2__Migrate_Users_To_Include_Usernames;
import org.flywaydb.core.Flyway;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdolFindConfigFileService extends FindConfigFileService<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {

    public static final String COMMUNITY_PROTOCOL = "find.community.protocol";
    public static final String COMMUNITY_HOST = "find.community.host";
    public static final String COMMUNITY_PORT = "find.community.port";

    private final AciService aciService;
    private final ProcessorFactory processorFactory;
    private final Flyway flyway;

    @Autowired
    public IdolFindConfigFileService(
            final FilterProvider filterProvider,
            final TextEncryptor textEncryptor,
            final JsonSerializer<FieldPath> fieldPathSerializer,
            final JsonDeserializer<FieldPath> fieldPathDeserializer,
            final AciHttpClient aciHttpClient,
            final ProcessorFactory processorFactory,
            final Flyway flyway
    ) {
        super(filterProvider, textEncryptor, fieldPathSerializer, fieldPathDeserializer);
        this.processorFactory = processorFactory;
        this.flyway = flyway;

        // TODO: eliminate circular dependency so we don't need to create this
        aciService = new AciServiceImpl(aciHttpClient);
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
    public void postUpdate(final IdolFindConfig config) {
        final CommunityAuthentication community = config.getLogin();

        if (community.validate(aciService, processorFactory).isValid()) {
            final AciServerDetails serverDetails = config.getCommunityDetails();

            // terrible hack - using system properties to pass data to migration
            System.setProperty(COMMUNITY_PROTOCOL, serverDetails.getProtocol().toString());
            System.setProperty(COMMUNITY_HOST, serverDetails.getHost());
            System.setProperty(COMMUNITY_PORT, String.valueOf(serverDetails.getPort()));

            flyway.migrate();

            System.clearProperty(COMMUNITY_PROTOCOL);
            System.clearProperty(COMMUNITY_HOST);
            System.clearProperty(COMMUNITY_PORT);
        }
    }

    @Override
    protected Map<Class<?>, Class<?>> customMixins() {
        return ImmutableMap.<Class<?>, Class<?>>builder()
                .put(Authentication.class, IdolAuthenticationMixins.class)
                .put(ServerConfig.class, ConfigurationFilterMixin.class)
                .put(ViewConfig.class, ConfigurationFilterMixin.class)
                .put(IdolFindConfig.class, ConfigurationFilterMixin.class)
                .put(FieldInfo.class, FieldInfoConfigMixins.class)
                .put(FieldValue.class, FieldValueConfigMixins.class)
                .build();
    }
}
