/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CustomisationAdminControllerIT extends AbstractFindIT {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws IOException {
        // remove the test files after each test
        new File(AbstractFindIT.TEST_DIR + "/customizations/logos/logo.png").delete();
    }

    @Test
    public void postLogo() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        final RequestBuilder requestBuilder = fileUpload(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", is("SUCCESS")));
    }

    @Test
    public void cannotUploadNonImageFiles() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "text/plain", "foo".getBytes());

        final RequestBuilder requestBuilder = fileUpload(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", is("INVALID_FILE")));
    }

    @Test
    public void errorWithNoFile() throws Exception {
        final RequestBuilder requestBuilder = fileUpload(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void logos() throws Exception {
        final RequestBuilder requestBuilder = get(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", empty()));
    }

    @Test
    public void getLogo() throws Exception {
        final byte[] fileContent = "foo".getBytes();
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", fileContent);

        final RequestBuilder postRequest = fileUpload(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(postRequest)
            .andExpect(status().isCreated());

        final RequestBuilder getRequest = get(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH + "/logo.png")
            .with(authentication(adminAuth()));

        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(fileContent));
    }

    @Test
    public void deleteLogo() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        final RequestBuilder postRequest = fileUpload(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(postRequest)
            .andExpect(status().isCreated());

        final RequestBuilder delete = delete(CustomisationAdminController.CUSTOMISATIONS_PATH + CustomisationAdminController.LOGO_PATH + "/logo.png")
            .with(authentication(adminAuth()));

        mockMvc.perform(delete)
            .andExpect(status().isNoContent());
    }

}