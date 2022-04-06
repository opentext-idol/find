/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractFindControllerIT extends AbstractFindIT {
    @Test
    public void index() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isFound());
    }

    @Test
    public void mainPage() throws Exception {
        mockMvc.perform(get(FindController.APP_PATH).with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    public void login() throws Exception {
        mockMvc.perform(get(FindController.LOGIN_PATH))
                .andExpect(status().isOk());
    }
}
