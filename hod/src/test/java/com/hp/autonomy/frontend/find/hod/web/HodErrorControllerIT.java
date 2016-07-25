/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringApplicationConfiguration(classes = HodFindApplication.class)
public class HodErrorControllerIT extends AbstractFindIT {
    @Test
    public void clientAuthenticationErrorPage() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH)
                .param(HodErrorController.STATUS_CODE_PARAM, String.valueOf(HttpStatus.SC_FORBIDDEN))
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder).andExpect(status().isOk());
    }
}
