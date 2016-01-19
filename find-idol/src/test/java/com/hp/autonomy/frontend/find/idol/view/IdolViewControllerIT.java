/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("ProhibitedExceptionDeclared")
@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolViewControllerIT extends AbstractFindIT {
    private static final String SAMPLE_REFERENCE = "http://starwars.wikia.com/wiki/Dagger Flight";
    private static final String SAMPLE_INDEX = "Wookiepedia";

    @Test
    public void viewDocument() throws Exception {
        mockMvc.perform(get(IdolViewController.VIEW_PATH + IdolViewController.VIEW_DOCUMENT_PATH).param(IdolViewController.REFERENCE_PARAM, SAMPLE_REFERENCE).param(IdolViewController.INDEX_PARAM, SAMPLE_INDEX))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    public void viewNonExistentDocument() throws Exception {
        mockMvc.perform(get(IdolViewController.VIEW_PATH + IdolViewController.VIEW_DOCUMENT_PATH).param(IdolViewController.REFERENCE_PARAM, "bad document").param(IdolViewController.INDEX_PARAM, SAMPLE_INDEX))
                .andExpect(status().isNotFound());
    }
}
