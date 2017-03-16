/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.junit.Test;
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

public class IdolDashboardControllerIT extends AbstractFindIT {
    private static final String UUID = "b1c71fad-a52d-47bf-a121-f71500bd7ddb";
    private static final String dashboardConfigPath = TEST_DIR + "/customization/dashboards.json";
    private static final String replacementDashboardConfigPath = "target/classes/dashboards-it.json";

    @Test
    public void testReloadConfig() throws Exception {
        final String replacementConfig = new String(Files.readAllBytes(Paths.get(replacementDashboardConfigPath)), "UTF-8");

        assertTrue("Replacement config contains UUID", replacementConfig.contains(UUID));

        currentConfigContainsUUID(false);

        // Replace current config file
        copyFileReplaceExisting();

        // Trigger config reload
        mockMvc.perform(
                get(IdolDashboardController.DASHBOARD_CONFIG_RELOAD_PATH)
                        .with(authentication(adminAuth()))
                        .header("referer", "http://abc.xyz"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://abc.xyz"));

        currentConfigContainsUUID(true);
    }

    private void currentConfigContainsUUID(final boolean condition) throws Exception {
        final RequestBuilder requestToAppPath = get(FindController.APP_PATH)
                .with(authentication(adminAuth()));

        mockMvc.perform(requestToAppPath)
                .andExpect(status().isOk())
                .andDo(mvcResult -> {
                    final String response = mvcResult.getResponse().getContentAsString();
                    assertEquals(response.contains(UUID), condition);
                });
    }

    private void copyFileReplaceExisting() throws IOException {
        final Path from = Paths.get(replacementDashboardConfigPath);
        final Path to = Paths.get(dashboardConfigPath);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
}
