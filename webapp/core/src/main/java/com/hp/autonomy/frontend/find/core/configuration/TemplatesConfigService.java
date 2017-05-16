package com.hp.autonomy.frontend.find.core.configuration;

import org.springframework.stereotype.Service;

@Service
public class TemplatesConfigService extends CustomizationConfigService<TemplatesConfig> {
    public TemplatesConfigService() {
        super(
                "templates.json",
                "defaultTemplatesConfigFile.json",
                TemplatesConfig.class,
                TemplatesConfig.builder().build()
        );
    }
}
