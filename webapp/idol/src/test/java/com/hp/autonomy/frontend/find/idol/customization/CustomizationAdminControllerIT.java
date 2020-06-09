/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SuppressWarnings("ProhibitedExceptionDeclared")
public class CustomizationAdminControllerIT extends AbstractFindIT {
    private static final AssetType ASSET_TYPE = AssetType.BIG_LOGO;
    private final String TARGET_FILE = AbstractFindIT.TEST_DIR + "/customization/assets/" + ASSET_TYPE.getDirectory() + "/logo.png";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws IOException {
        // remove the test files after each test
        new File(TARGET_FILE).delete();
    }

    @Test
    public void postLogo() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        final RequestBuilder requestBuilder = fileUpload(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
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

        final RequestBuilder requestBuilder = fileUpload(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", is("INVALID_FILE")));
    }

    @Test
    public void errorWithNoFile() throws Exception {
        final RequestBuilder requestBuilder = fileUpload(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void logos() throws Exception {
        final RequestBuilder requestBuilder = get(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
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

        final RequestBuilder postRequest = fileUpload(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(postRequest)
            .andExpect(status().isCreated());

        final RequestBuilder getRequest = get(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE + '/' + "/logo.png")
            .with(authentication(adminAuth()));

        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(fileContent));
    }

    @Test
    public void deleteLogo() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        final RequestBuilder postRequest = fileUpload(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE)
            .file(multipartFile)
            .with(authentication(adminAuth()));

        mockMvc.perform(postRequest)
            .andExpect(status().isCreated());

        final RequestBuilder delete = delete(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.ASSETS_PATH + '/' + ASSET_TYPE + '/' + "/logo.png")
            .with(authentication(adminAuth()));

        mockMvc.perform(delete)
            .andExpect(status().isNoContent());
    }

    @Test
    public void updateConfig() throws Exception {
        final Path path = Paths.get(TARGET_FILE);
        Files.createDirectories(path.getParent());
        Files.createFile(path);

        final RequestBuilder requestBuilder = post(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.CONFIG_PATH)
            .content("{\"assets\":{\"BIG_LOGO\": \"logo.png\"}}")
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent());
    }

    @Test
    public void updateConfigWithBadFile() throws Exception {
        final RequestBuilder requestBuilder = post(CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH + CustomizationAdminController.CONFIG_PATH)
            .content("{\"assets\":{\"BIG_LOGO\": \"logo.png\"}}")
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(adminAuth()));

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNotAcceptable());
    }
}
