/*
 * Copyright 2015 Open Text.
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

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("ProhibitedExceptionDeclared")
public abstract class AbstractViewControllerIT extends AbstractFindIT {
    @Test
    public void viewDocument() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        final MockHttpServletRequestBuilder requestBuilder = get(ViewController.VIEW_PATH + ViewController.VIEW_DOCUMENT_PATH)
                .param(ViewController.REFERENCE_PARAM, reference)
                .param(ViewController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0])
                .param(ViewController.PART_PARAM, "DOCUMENT")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}
