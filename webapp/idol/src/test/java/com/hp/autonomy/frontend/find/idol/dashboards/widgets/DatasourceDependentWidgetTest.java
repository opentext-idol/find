/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.ResolvableType;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public abstract class DatasourceDependentWidgetTest<W extends Widget<W, WS> & DatasourceDependentWidget, WS extends WidgetSettings<WS>> extends ConfigurationComponentTest<W> {
    @Override
    public void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class);
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Test
    public void noDatasource() throws ConfigException {
        try {
            constructComponentWithoutDatasource().basicValidate(null);
            fail("Exception should have been thrown");
        } catch(final ConfigException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Datasource must be specified"));
        }
    }

    abstract W constructComponentWithoutDatasource();
}
