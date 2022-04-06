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

package com.hp.autonomy.frontend.find.idol.view;

import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerIT;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdolViewControllerIT extends AbstractViewControllerIT {
    @Test
    public void viewNonExistentDocument() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ViewController.VIEW_PATH + ViewController.VIEW_DOCUMENT_PATH)
                .param(ViewController.REFERENCE_PARAM, "bad document")
                .param(ViewController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0])
                .param(ViewController.ORIGINAL_PARAM, "false")
                .with(authentication(userAuth()));

        mockMvc.perform(
                requestBuilder)
                .andExpect(status().isNotFound());
    }
}
