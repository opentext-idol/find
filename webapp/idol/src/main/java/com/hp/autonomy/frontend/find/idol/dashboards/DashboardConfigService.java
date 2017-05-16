/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.TagNameSerializer;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.Widget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.WidgetMixins;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Service
public class DashboardConfigService extends CustomizationConfigService<DashboardConfig> {
    @Autowired
    public DashboardConfigService(final JsonDeserializer<TagName> tagNameDeserializer) {
        super(
            "dashboards.json",
            "defaultDashboardsConfigFile.json",
            DashboardConfig.class,
            DashboardConfig.builder().build(),
            new Jackson2ObjectMapperBuilder()
                .mixIn(Widget.class, WidgetMixins.class)
                .mixIn(WidgetDatasource.class, WidgetDatasourceMixins.class)
                .deserializersByType(ImmutableMap.of(TagName.class, tagNameDeserializer))
                .serializersByType(ImmutableMap.of(TagName.class, new TagNameSerializer()))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                   DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        );
    }
}
