package com.hp.autonomy.frontend.selenium.base;

import com.hp.autonomy.frontend.selenium.config.Browser;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.config.json.JsonConfig;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;

import java.util.ArrayList;
import java.util.List;

public class TestParameterFactory implements ParametrizedFactory<JsonConfig, List<Object[]>> {
    @Override
    public List<Object[]> create(JsonConfig context) {
        List<Object[]> configs = new ArrayList<>();
        for (Browser browser : context.getBrowsers()) {
            configs.add(new Object[] { new TestConfig(context, browser) });
        }
        return configs;
    }
}
