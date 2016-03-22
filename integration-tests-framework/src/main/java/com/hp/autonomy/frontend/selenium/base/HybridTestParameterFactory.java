package com.hp.autonomy.frontend.selenium.base;

import com.hp.autonomy.frontend.selenium.config.json.JsonConfig;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HybridTestParameterFactory extends TestParameterFactory {
    private final Collection<ApplicationType> allowedTypes;

    public HybridTestParameterFactory(Collection<ApplicationType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    @Override
    public List<Object[]> create(JsonConfig context) {
        if (isAllowed(context)) {
            return super.create(context);
        }
        return Collections.emptyList();
    }

    private boolean isAllowed(JsonConfig context) {
        return allowedTypes.contains(context.getAppType());
    }
}
