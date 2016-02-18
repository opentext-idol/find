/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractErrorControllerIT extends AbstractFindIT {
    @Test
    public void authenticationErrorPage() throws Exception {
        mockMvc.perform(get(DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH))
                .andExpect(status().isOk());
    }

    @Test
    public void clientAuthenticationErrorPage() throws Exception {
        mockMvc.perform(get(DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH).param(CustomErrorController.STATUS_CODE_PARAM, String.valueOf(HttpStatus.SC_FORBIDDEN)))
                .andExpect(status().isOk());
    }

    @Test
    public void serverErrorPage() throws Exception {
        mockMvc.perform(get(DispatcherServletConfiguration.SERVER_ERROR_PATH))
                .andExpect(status().isOk());
    }

    @Test
    public void notFoundError() throws Exception {
        mockMvc.perform(get(DispatcherServletConfiguration.NOT_FOUND_ERROR_PATH))
                .andExpect(status().isOk());
    }
}
