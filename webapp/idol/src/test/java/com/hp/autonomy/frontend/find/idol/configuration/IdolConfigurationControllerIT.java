/*
 * Copyright 2026 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.idol.api.ConfigurationApi;
import org.junit.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdolConfigurationControllerIT extends AbstractFindIT {

    @Test
    public void testGetConfigAsAdmin() throws Exception {
        mockMvc.perform(get(ConfigurationApi.PATH_GET_CONFIG)
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetConfigAsConfigUser() throws Exception {
        mockMvc.perform(get(ConfigurationApi.PATH_GET_CONFIG)
                        .with(authentication(configAuth())))
                .andExpect(status().isOk());
    }

    @Test
    public void testConfigUserCannotReachOtherAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/customization/assets/BIG_LOGO")
                        .with(authentication(configAuth())))
                .andExpect(status().isForbidden());
    }

}
