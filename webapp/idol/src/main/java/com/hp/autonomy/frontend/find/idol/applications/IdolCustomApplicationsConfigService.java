/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.applications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class IdolCustomApplicationsConfigService extends BaseConfigFileService<IdolCustomApplicationsConfig> {
    public IdolCustomApplicationsConfigService() {
        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .createXmlMapper(false)
                .build();

        setMapper(objectMapper);
        setConfigFileLocation(FindConfigFileService.CONFIG_FILE_LOCATION);
        setConfigFileName("customization/applications.json");
        setDefaultConfigFile("/defaultApplicationsConfigFile.json");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void init() throws Exception {
        // need to make sure the sub directory exists
        Files.createDirectories(Paths.get(getConfigFileLocation()).getParent());
        super.init();
    }

    /**
     * Called after the Config is initialised
     *
     * @param config The newly initialised config
     * @throws Exception any error
     */
    @Override
    public void postInitialise(final IdolCustomApplicationsConfig config) throws Exception {}

    /**
     * @return The class object representing T.
     */
    @Override
    public Class<IdolCustomApplicationsConfig> getConfigClass() {
        return IdolCustomApplicationsConfig.class;
    }

    /**
     * Returns a configuration object on which no properties have been set.
     *
     * @return An empty configuration object.
     */
    @Override
    public IdolCustomApplicationsConfig getEmptyConfig() {
        return IdolCustomApplicationsConfig.builder().build();
    }

    /**
     * Generates a default login for a new config file
     *
     * @param config The initial config object
     * @return A copy of config with a default login, or the same config object if a default login is not required
     */
    @Override
    public IdolCustomApplicationsConfig generateDefaultLogin(final IdolCustomApplicationsConfig config) {
        return config;
    }

    /**
     * Removes the default login from the configuration object
     *
     * @param config The initial config object
     * @return A copy of config without a default login, or the same config object if a default login is not required
     */
    @Override
    public IdolCustomApplicationsConfig withoutDefaultLogin(final IdolCustomApplicationsConfig config) {
        return config;
    }

    /**
     * Hashes any passwords in the configuration object
     *
     * @param config The initial config object
     * @return A copy of config without any plaintext passwords, or the same config object if there are no passwords
     */
    @Override
    public IdolCustomApplicationsConfig withHashedPasswords(final IdolCustomApplicationsConfig config) {
        return config;
    }

    @Override
    public IdolCustomApplicationsConfig preUpdate(final IdolCustomApplicationsConfig idolCustomApplicationsConfig) {
        return idolCustomApplicationsConfig;
    }

    @Override
    public void postUpdate(final IdolCustomApplicationsConfig idolCustomApplicationsConfig) throws Exception {}
}
