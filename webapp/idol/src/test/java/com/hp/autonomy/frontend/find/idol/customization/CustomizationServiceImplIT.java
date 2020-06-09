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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CustomizationServiceImplIT extends AbstractFindIT {
    private static final AssetType ASSET_TYPE = AssetType.BIG_LOGO;

    @Autowired
    private CustomizationServiceImpl customizationService;

    @After
    public void tearDown() {
        customizationService.deleteAsset(ASSET_TYPE, "aardvark.png");
        customizationService.deleteAsset(ASSET_TYPE, "bee.png");
        customizationService.deleteAsset(ASSET_TYPE, "logo.png");
        customizationService.deleteAsset(ASSET_TYPE, "sea.png");
    }

    @Test
    public void testSaveLogo() throws CustomizationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customizationService.saveAsset(ASSET_TYPE, multipartFile, false);

        final File logo = customizationService.getAsset(ASSET_TYPE, "logo.png");

        assertThat(logo.length(), is(3L));
    }

    @Test
    public void testGetLogos() throws CustomizationException {
        final List<String> logos = customizationService.getAssets(ASSET_TYPE);

        assertThat(logos, is(empty()));
    }

    @Test
    public void testGetLogosIsSorted() throws CustomizationException {
        final List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("sea.png", "sea.png", "image/png", "foo".getBytes()),
            new MockMultipartFile("aardvark.png", "aardvark.png", "image/png", "foo".getBytes()),
            new MockMultipartFile("bee.png", "bee.png", "image/png", "foo".getBytes())
        );

        for(final MultipartFile file : files) {
            customizationService.saveAsset(ASSET_TYPE, file, false);
        }

        final List<String> logos = customizationService.getAssets(ASSET_TYPE);

        assertThat(logos, contains("aardvark.png", "bee.png", "sea.png"));
    }

    @Test
    public void testGetLogo() {
        final File logo = customizationService.getAsset(ASSET_TYPE, "fake.png");

        assertThat(logo.exists(), is(false));
    }

    @Test
    public void testDeleteLogo() throws CustomizationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customizationService.saveAsset(ASSET_TYPE, multipartFile, false);

        final boolean deleted = customizationService.deleteAsset(ASSET_TYPE, "logo.png");

        assertThat(deleted, is(true));

        final File logo = customizationService.getAsset(ASSET_TYPE, "logo.png");

        assertThat(logo.exists(), is(false));
    }

    @Test
    public void testIOExceptions() {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes()) {
            @Override
            public void transferTo(final File dest) throws IOException, IllegalStateException {
                throw new IOException("You are writing to /dev/null");
            }
        };

        try {
            customizationService.saveAsset(ASSET_TYPE, multipartFile, false);

            fail("CustomizationException not thrown");
        } catch(final CustomizationException e) {
            assertThat(e.getStatus(), is(Status.IO_ERROR));
        }
    }

    @Test
    public void testOverwriting() {
        final MultipartFile file1 = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        try {
            customizationService.saveAsset(ASSET_TYPE, file1, false);
        } catch(final CustomizationException e) {
            fail("File should not exist yet");
        }

        final MultipartFile file2 = new MockMultipartFile("file", "logo.png", "image/png", "barr".getBytes());

        try {
            customizationService.saveAsset(ASSET_TYPE, file2, false);

            fail();
        } catch(final CustomizationException e) {
            assertThat(e.getStatus(), is(Status.FILE_EXISTS));
        }

        try {
            customizationService.saveAsset(ASSET_TYPE, file2, true);
        } catch(final CustomizationException e) {
            fail("File should have been overwritten");
        }

        assertThat(customizationService.getAsset(ASSET_TYPE, "logo.png").length(), is(4L));
    }
}
