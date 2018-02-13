/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.find.core.beanconfiguration.ConfigFileConfiguration;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.text.TextEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.File;
import java.io.IOException;

import static com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService.CONFIG_FILE_LOCATION;
import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY)
public abstract class FindConfigFileServiceTest<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> {
    @ClassRule
    public static final SpringClassRule SCR = new SpringClassRule();
    private static final String TEST_DIR = "./target/test";
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    @Autowired
    protected FieldPathNormaliser fieldPathNormaliser;
    @Autowired
    protected JsonSerializer<FieldPath> fieldPathSerializer;
    @Autowired
    protected JsonDeserializer<FieldPath> fieldPathDeserializer;
    @Mock
    protected TextEncryptor textEncryptor;
    protected FilterProvider filterProvider;
    protected FindConfigFileService<C, B> findConfigFileService;

    @BeforeClass
    public static void init() {
        System.setProperty(CONFIG_FILE_LOCATION, TEST_DIR);
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Before
    public void setUp() throws Exception {
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);

        filterProvider = new ConfigFileConfiguration().filterProvider();

        findConfigFileService = constructConfigFileService();
        findConfigFileService.init();
    }

    protected abstract FindConfigFileService<C, B> constructConfigFileService();

    protected abstract Class<C> getConfigClassType();

    protected abstract void validateConfig(final String configFileContents);

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Test
    public void configFile() throws IOException {
        final File generatedConfigFile = new File(TEST_DIR, "config.json");
        assertTrue(generatedConfigFile.exists());

        final String configFileContents = FileUtils.readFileToString(generatedConfigFile);
        validateConfig(configFileContents);
    }

    @Test
    public void getConfig() {
        final C config = findConfigFileService.getConfig();
        assertNotNull(config);
    }

    @Test
    public void getConfigClass() {
        assertEquals(getConfigClassType(), findConfigFileService.getConfigClass());
    }

    @Test
    public void getEmptyConfig() {
        assertNotNull(findConfigFileService.getEmptyConfig());
    }

    @Test
    public void postInitialise() {
        // Check no exceptions are thrown
        final C config = findConfigFileService.getConfig();
        findConfigFileService.postInitialise(config);
    }

    @Test
    public void preUpdate() {
        final C config = findConfigFileService.getConfig();
        assertEquals(config, findConfigFileService.preUpdate(config));
    }

    @Test
    public void postUpdate() {
        // Check no exceptions are thrown
        final C config = findConfigFileService.getConfig();
        findConfigFileService.postUpdate(config);
    }
}
