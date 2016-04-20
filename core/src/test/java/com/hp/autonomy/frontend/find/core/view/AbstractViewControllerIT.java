/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("ProhibitedExceptionDeclared")
public abstract class AbstractViewControllerIT extends AbstractFindIT {
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;

    @Test
    public void viewDocument() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        mockMvc.perform(
                get(ViewController.VIEW_PATH + ViewController.VIEW_DOCUMENT_PATH)
                        .param(ViewController.REFERENCE_PARAM, reference)
                        .param(ViewController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0]))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}
