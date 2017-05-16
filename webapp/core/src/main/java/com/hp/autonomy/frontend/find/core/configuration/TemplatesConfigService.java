package com.hp.autonomy.frontend.find.core.configuration;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TemplatesConfigService extends CustomisationConfigService<TemplatesConfig> {
    public TemplatesConfigService() throws IOException {
        super(
                "templates.json",
                "defaultTemplatesConfigFile.json",
                TemplatesConfig.class,
                TemplatesConfig.builder().build()
        );
    }
}
