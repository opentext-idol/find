/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.text.TextEncryptor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class IdolFindConfigFileServiceTest {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("hp.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
        FileUtils.copyFileToDirectory(new File("./src/test/resources/config.json"), directory);
    }

    @AfterClass
    public static void destroy() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Mock
    private TextEncryptor textEncryptor;

    @Mock
    private FilterProvider filterProvider;

    private IdolFindConfigFileService idolFindConfigFileService;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Before
    public void setUp() throws Exception {
        idolFindConfigFileService = new IdolFindConfigFileService();
        idolFindConfigFileService.setConfigFileLocation("hp.find.home");
        idolFindConfigFileService.setConfigFileName("config.json");
        idolFindConfigFileService.setDefaultConfigFile("/defaultIdolConfigFile.json");
        idolFindConfigFileService.setMapper(new ObjectMapper());
        idolFindConfigFileService.setTextEncryptor(textEncryptor);
        idolFindConfigFileService.setFilterProvider(filterProvider);
        idolFindConfigFileService.init();
    }

    @Test
    public void getConfig() {
        assertNotNull(idolFindConfigFileService.getConfig());
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
