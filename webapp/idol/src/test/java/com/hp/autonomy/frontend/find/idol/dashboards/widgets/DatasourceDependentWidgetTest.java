package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.ResolvableType;

public abstract class DatasourceDependentWidgetTest<W extends Widget<W, WS> & DatasourceDependentWidget, WS extends WidgetSettings<WS>> extends ConfigurationComponentTest<W> {
    @Override
    public void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class);
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Test(expected = ConfigException.class)
    public void noDatasource() throws ConfigException {
        constructComponentWithoutDatasource().basicValidate(null);
    }

    abstract W constructComponentWithoutDatasource();
}
