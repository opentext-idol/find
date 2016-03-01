/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;

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
        mockMvc.perform(get(FindController.PUBLIC_PATH))
                .andExpect(status().isOk());
    }

    @Test
    public void login() throws Exception {
        mockMvc.perform(get(FindController.LOGIN_PATH))
                .andExpect(status().isOk());
    }
}
