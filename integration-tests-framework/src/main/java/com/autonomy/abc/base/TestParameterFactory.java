package com.autonomy.abc.base;

import com.autonomy.abc.config.Browser;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.config.json.JsonConfig;
import com.autonomy.abc.selenium.util.ParametrizedFactory;

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
