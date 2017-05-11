package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Service
public class TemplatesConfigService extends BaseConfigFileService<TemplatesConfig>{
    public TemplatesConfigService() {
        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .createXmlMapper(false)
                .build();

        setMapper(objectMapper);
        setConfigFileLocation(FindConfigFileService.CONFIG_FILE_LOCATION);
        setConfigFileName("customization/templates/templates.json");
        setDefaultConfigFile("/defaultTemplatesConfigFile.json");
    }

    @Override
    public void postInitialise(final TemplatesConfig config) throws Exception {}

    @Override
    public Class<TemplatesConfig> getConfigClass() {
        return TemplatesConfig.class;
    }

    @Override
    public TemplatesConfig getEmptyConfig() {
        return TemplatesConfig.builder().build();
    }

    @Override
    public TemplatesConfig generateDefaultLogin(final TemplatesConfig config) {
        return config;
    }

    @Override
    public TemplatesConfig withoutDefaultLogin(final TemplatesConfig config) {
        return config;
    }

    @Override
    public TemplatesConfig withHashedPasswords(final TemplatesConfig config) {
        return config;
    }

    @Override
    public TemplatesConfig preUpdate(final TemplatesConfig config) {
        return config;
    }

    @Override
    public void postUpdate(final TemplatesConfig config) throws Exception {}
}
