/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.customization.templates;

import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.Template;
import com.hp.autonomy.frontend.find.core.configuration.TemplatesConfig;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplatesServiceImplIT {
    private static final Path HOME_DIR = Paths.get("target", "test").toAbsolutePath();

    private static final Path TEMPLATES_DIR = HOME_DIR.resolve(Paths.get(
            CustomizationConfigService.CONFIG_DIRECTORY,
            TemplatesServiceImpl.DIRECTORY_NAME
    ));

    @Mock
    private CustomizationConfigService<TemplatesConfig> configService;

    private TemplatesServiceImpl templatesService;

    @Before
    public void setUp() throws IOException, TemplateNotFoundException {
        Files.createDirectories(TEMPLATES_DIR);
        Files.write(TEMPLATES_DIR.resolve("preview.html"), "<p>Preview</p>".getBytes(StandardCharsets.UTF_8));
        Files.write(TEMPLATES_DIR.resolve("person.html"), "<p>Person</p>".getBytes(StandardCharsets.UTF_8));
        Files.write(TEMPLATES_DIR.resolve("document.html"), "<p>Document</p>".getBytes(StandardCharsets.UTF_8));
        Files.write(TEMPLATES_DIR.resolve("animal.html"), "<p>Animal</p>".getBytes(StandardCharsets.UTF_8));

        templatesService = new TemplatesServiceImpl(configService, HOME_DIR.toString());
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        FileUtils.deleteDirectory(HOME_DIR.toFile());
    }

    @Test
    public void loadAndGetTemplates() throws IOException, TemplateNotFoundException {
        when(configService.getConfig()).thenReturn(initialConfig());

        templatesService.loadTemplates();

        final Templates templates = templatesService.getTemplates();
        assertThat(templates.getLastModified(), not(nullValue()));

        final Map<String, String> templateMap = templates.getTemplates();
        assertThat(templateMap.entrySet(), hasSize(3));
        assertThat(templateMap, hasEntry("preview.html", "<p>Preview</p>"));
        assertThat(templateMap, hasEntry("person.html", "<p>Person</p>"));
        assertThat(templateMap, hasEntry("document.html", "<p>Document</p>"));
    }

    @Test
    public void loadMissingTemplate() throws IOException {
        when(configService.getConfig()).thenReturn(initialConfig());

        Files.delete(TEMPLATES_DIR.resolve("person.html"));

        try {
            templatesService.loadTemplates();
            fail("Expected a TemplateNotFoundException");
        } catch (final TemplateNotFoundException e) {
            assertThat(e.getMessage(), containsString("person.html"));
        }
    }

    @Test
    public void reloadTemplates() throws Exception {
        final TemplatesConfig secondConfig = TemplatesConfig.builder()
                .previewPanel(Arrays.asList(
                        template("animal.html"),
                        template("preview.html")
                ))
                .searchResult(Collections.singletonList(
                        template("document.html")
                ))
                .build();

        when(configService.getConfig())
                .thenReturn(initialConfig())
                .thenReturn(secondConfig);

        templatesService.loadTemplates();
        templatesService.reload();

        final Templates templatesAndLastModified = templatesService.getTemplates();
        assertThat(templatesAndLastModified.getLastModified(), not(nullValue()));

        final Map<String, String> templates = templatesAndLastModified.getTemplates();
        assertThat(templates.entrySet(), hasSize(3));
        assertThat(templates, hasEntry("preview.html", "<p>Preview</p>"));
        assertThat(templates, hasEntry("animal.html", "<p>Animal</p>"));
        assertThat(templates, hasEntry("document.html", "<p>Document</p>"));
    }

    private TemplatesConfig initialConfig() {
        return TemplatesConfig.builder()
                .previewPanel(Collections.singletonList(
                        template("preview.html")
                ))
                .searchResult(Arrays.asList(
                        template("person.html"),
                        template("document.html")
                ))
                .build();
    }

    private Template template(final String fileName) {
        return Template.builder().file(fileName).build();
    }
}
