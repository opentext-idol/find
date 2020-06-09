/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
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
public class DashboardConfigService extends CustomizationConfigService<DashboardConfig> implements ReloadableCustomizationComponent {
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

    @Override
    public void reload() throws Exception {
        init();
    }
}
