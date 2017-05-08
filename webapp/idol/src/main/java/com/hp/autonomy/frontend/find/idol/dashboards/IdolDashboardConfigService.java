/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.TagNameSerializer;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.Widget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.WidgetMixins;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class IdolDashboardConfigService extends BaseConfigFileService<IdolDashboardConfig> {
    @Autowired
    public IdolDashboardConfigService(final JsonDeserializer<TagName> tagNameDeserializer) {
        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .mixIn(Widget.class, WidgetMixins.class)
                .mixIn(WidgetDatasource.class, WidgetDatasourceMixins.class)
                .deserializersByType(ImmutableMap.of(TagName.class, tagNameDeserializer))
                .serializersByType(ImmutableMap.of(TagName.class, new TagNameSerializer()))
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .createXmlMapper(false)
                .build();

        setMapper(objectMapper);
        setConfigFileLocation(FindConfigFileService.CONFIG_FILE_LOCATION);
        setConfigFileName("customization/dashboards.json");
        setDefaultConfigFile("/defaultDashboardsConfigFile.json");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void init() throws Exception {
        // need to make sure the sub directory exists
        Files.createDirectories(Paths.get(getConfigFileLocation()).getParent());
        super.init();
    }

    @Override
    public IdolDashboardConfig getEmptyConfig() {
        return IdolDashboardConfig.builder().build();
    }

    @Override
    public Class<IdolDashboardConfig> getConfigClass() {
        return IdolDashboardConfig.class;
    }

    @Override
    public IdolDashboardConfig generateDefaultLogin(final IdolDashboardConfig config) {
        return config;
    }

    @Override
    public IdolDashboardConfig withoutDefaultLogin(final IdolDashboardConfig config) {
        return config;
    }

    @Override
    public IdolDashboardConfig withHashedPasswords(final IdolDashboardConfig config) {
        return config;
    }

    @Override
    public IdolDashboardConfig preUpdate(final IdolDashboardConfig idolDashboardConfig) {
        return idolDashboardConfig;
    }

    @Override
    public void postInitialise(final IdolDashboardConfig config) {}

    @Override
    public void postUpdate(final IdolDashboardConfig idolDashboardConfig) {}
}
