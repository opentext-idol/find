/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

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

public class CustomisationServiceImplIT extends AbstractFindIT {

    private static final AssetType ASSET_TYPE = AssetType.BIG_LOGO;

    @Autowired
    private CustomisationServiceImpl customisationService;

    @After
    public void tearDown() {
        customisationService.deleteAsset(ASSET_TYPE, "aardvark.png");
        customisationService.deleteAsset(ASSET_TYPE, "bee.png");
        customisationService.deleteAsset(ASSET_TYPE, "logo.png");
        customisationService.deleteAsset(ASSET_TYPE, "sea.png");
    }

    @Test
    public void testSaveLogo() throws CustomisationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customisationService.saveAsset(ASSET_TYPE, multipartFile, false);

        final File logo = customisationService.getAsset(ASSET_TYPE, "logo.png");

        assertThat(logo.length(), is(3L));
    }

    @Test
    public void testGetLogos() throws CustomisationException {
        final List<String> logos = customisationService.getAssets(ASSET_TYPE);

        assertThat(logos, is(empty()));
    }

    @Test
    public void testGetLogosIsSorted() throws CustomisationException {
        final List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("sea.png", "sea.png", "image/png", "foo".getBytes()),
            new MockMultipartFile("aardvark.png", "aardvark.png", "image/png", "foo".getBytes()),
            new MockMultipartFile("bee.png", "bee.png", "image/png", "foo".getBytes())
        );

        for (final MultipartFile file : files) {
            customisationService.saveAsset(ASSET_TYPE, file, false);
        }

        final List<String> logos = customisationService.getAssets(ASSET_TYPE);

        assertThat(logos, contains("aardvark.png", "bee.png", "sea.png"));
    }

    @Test
    public void testGetLogo() {
        final File logo = customisationService.getAsset(ASSET_TYPE, "fake.png");

        assertThat(logo.exists(), is(false));
    }

    @Test
    public void testDeleteLogo() throws CustomisationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customisationService.saveAsset(ASSET_TYPE, multipartFile, false);

        final boolean deleted = customisationService.deleteAsset(ASSET_TYPE, "logo.png");

        assertThat(deleted, is(true));

        final File logo = customisationService.getAsset(ASSET_TYPE, "logo.png");

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
            customisationService.saveAsset(ASSET_TYPE, multipartFile, false);

            fail("CustomisationException not thrown");
        } catch (final CustomisationException e) {
            assertThat(e.getStatus(), is(Status.IO_ERROR));
        }
    }

    @Test
    public void testOverwriting() {
        final MultipartFile file1 = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        try {
            customisationService.saveAsset(ASSET_TYPE, file1, false);
        } catch (final CustomisationException e) {
            fail("File should not exist yet");
        }

        final MultipartFile file2 = new MockMultipartFile("file", "logo.png", "image/png", "barr".getBytes());

        try {
            customisationService.saveAsset(ASSET_TYPE, file2, false);

            fail();
        } catch (final CustomisationException e) {
            assertThat(e.getStatus(), is(Status.FILE_EXISTS));
        }

        try {
            customisationService.saveAsset(ASSET_TYPE, file2, true);
        } catch (final CustomisationException e) {
            fail("File should have been overwritten");
        }

        assertThat(customisationService.getAsset(ASSET_TYPE, "logo.png").length(), is(4L));
    }

}