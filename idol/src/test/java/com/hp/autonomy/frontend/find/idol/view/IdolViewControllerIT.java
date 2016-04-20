/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerIT;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolViewControllerIT extends AbstractViewControllerIT {
    @Test
    public void viewNonExistentDocument() throws Exception {
        mockMvc.perform(
                get(ViewController.VIEW_PATH + ViewController.VIEW_DOCUMENT_PATH)
                        .param(ViewController.REFERENCE_PARAM, "bad document")
                        .param(ViewController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0]))
                .andExpect(status().isNotFound());
    }
}
