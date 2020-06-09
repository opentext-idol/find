/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.web.FindController;
import java.io.File;
import java.io.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@DirtiesContext
public class CustomizationReloadControllerIT extends AbstractFindIT {
    private static final String UUID = "b1c71fad-a52d-47bf-a121-f71500bd7ddb";
    private static final String DASHBOARD_CONFIG = TEST_DIR + "/customization/dashboards.json";
    private static final String DASHBOARD_CONFIG_BACKUP = TEST_DIR + "/customization/dashboards.json.bak";
    private static final String REPLACEMENT_CONFIG = "target/test-classes/DashboardControllerIT-Config-1.json";

    @BeforeClass
    public static void init() throws IOException {
        try {
            FileUtils.forceDelete(new File(TEST_DIR));
        }
        catch(FileNotFoundException e) {
            // do nothing, this is expected
        }

        AbstractFindIT.init();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        // Back up current config file

        copyFileReplaceExisting(DASHBOARD_CONFIG, DASHBOARD_CONFIG_BACKUP);
    }

    @After
    public void tearDown() {
        // Restore original config file, delete backup
        moveFileReplaceExisting(DASHBOARD_CONFIG_BACKUP, DASHBOARD_CONFIG);
    }

    @Test
    public void testReloadConfig() throws Exception {
        final String replacementConfigContents = new String(Files.readAllBytes(Paths.get(REPLACEMENT_CONFIG)), "UTF-8");
        assertTrue("Replacement config contains UUID", replacementConfigContents.contains(UUID));

        currentConfigContainsUUID(false);

        // Replace current config file
        copyFileReplaceExisting(REPLACEMENT_CONFIG, DASHBOARD_CONFIG);

        triggerConfigReload();

        currentConfigContainsUUID(true);
    }

    private void copyFileReplaceExisting(final String from, final String to) {
        final Path fromPath = Paths.get(from);
        final Path toPath = Paths.get(to);
        try {
            Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        } catch(final IOException e) {
            throw new IllegalStateException("Could not replace current config file", e);
        }
    }

    private void moveFileReplaceExisting(final String from, final String to) {
        final Path fromPath = Paths.get(from);
        final Path toPath = Paths.get(to);
        try {
            Files.move(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        } catch(final IOException e) {
            throw new IllegalStateException("Could not replace current config file", e);
        }
    }

    private void currentConfigContainsUUID(final boolean expected) throws Exception {
        final RequestBuilder requestToAppPath = get(FindController.APP_PATH)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestToAppPath)
            .andExpect(status().isOk())
            .andDo(mvcResult -> {
                final String response = mvcResult.getResponse().getContentAsString();
                assertEquals(expected, response.contains(UUID));
            });
    }

    private void triggerConfigReload() throws Exception {
        mockMvc.perform(
            get(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH +
                    CustomizationReloadController.CONFIG_RELOAD_PATH)
                .with(authentication(adminAuth())))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl(FindController.APP_PATH));
    }
}
