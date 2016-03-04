/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import com.hp.autonomy.frontend.find.core.beanconfiguration.ConfigFileConfiguration;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.text.TextEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolFindConfigFileServiceTest {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() {
        System.setProperty("hp.find.home", TEST_DIR);
    }

    @Mock
    private TextEncryptor textEncryptor;

    private IdolFindConfigFileService idolFindConfigFileService;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Before
    public void setUp() throws Exception {
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(ServerConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(ViewConfig.class, ConfigurationFilterMixin.class);

        final FilterProvider filterProvider = new ConfigFileConfiguration().filterProvider();

        idolFindConfigFileService = new IdolFindConfigFileService();
        idolFindConfigFileService.setConfigFileLocation("hp.find.home");
        idolFindConfigFileService.setConfigFileName("config.json");
        idolFindConfigFileService.setDefaultConfigFile("/defaultIdolConfigFile.json");
        idolFindConfigFileService.setMapper(objectMapper);
        idolFindConfigFileService.setTextEncryptor(textEncryptor);
        idolFindConfigFileService.setFilterProvider(filterProvider);
        idolFindConfigFileService.init();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Test
    public void configFile() throws IOException {
        final File generatedConfigFile = new File(TEST_DIR, "config.json");
        assertTrue(generatedConfigFile.exists());

        final String configFileContents = FileUtils.readFileToString(generatedConfigFile);
        assertFalse(configFileContents.contains("\"indexProtocol\""));
        assertFalse(configFileContents.contains("\"indexPort\""));
        assertFalse(configFileContents.contains("\"serviceProtocol\""));
        assertFalse(configFileContents.contains("\"servicePort\""));
        assertFalse(configFileContents.contains("\"productType\""));
        assertFalse(configFileContents.contains("\"indexErrorMessage\""));
    }

    @Test
    public void getConfig() {
        final IdolFindConfig config = idolFindConfigFileService.getConfig();
        assertNotNull(config);
    }

    @Test
    public void getConfigClass() {
        assertEquals(IdolFindConfig.class.getName(), idolFindConfigFileService.getConfigClass().getName());
    }

    @Test
    public void getEmptyConfig() {
        assertNotNull(idolFindConfigFileService.getEmptyConfig());
    }

    @Test
    public void initialisingMethods() {
        idolFindConfigFileService.postInitialise(null);
        idolFindConfigFileService.preUpdate(null);
        idolFindConfigFileService.postUpdate(null);
    }
}
