/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.After;
import org.junit.Before;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CustomisationServiceImplIT extends AbstractFindIT {

    @Autowired
    private CustomisationServiceImpl customisationService;

    @After
    public void tearDown() {
        customisationService.deleteLogo("aardvark.png");
        customisationService.deleteLogo("bee.png");
        customisationService.deleteLogo("logo.png");
        customisationService.deleteLogo("sea.png");
    }

    @Test
    public void testSaveLogo() throws CustomisationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customisationService.saveLogo(multipartFile, false);

        final File logo = customisationService.getLogo("logo.png");

        assertThat(logo.length(), is(3L));
    }

    @Test
    public void testGetLogos() throws CustomisationException {
        final List<String> logos = customisationService.getLogos();

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
            customisationService.saveLogo(file, false);
        }

        final List<String> logos = customisationService.getLogos();

        assertThat(logos, contains("aardvark.png", "bee.png", "sea.png"));
    }

    @Test
    public void testGetLogo() {
        final File logo = customisationService.getLogo("fake.png");

        assertThat(logo.exists(), is(false));
    }

    @Test
    public void testDeleteLogo() throws CustomisationException {
        final MultipartFile multipartFile = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        customisationService.saveLogo(multipartFile, false);

        final boolean deleted = customisationService.deleteLogo("logo.png");

        assertThat(deleted, is(true));

        final File logo = customisationService.getLogo("logo.png");

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
            customisationService.saveLogo(multipartFile, false);

            fail("CustomisationException not thrown");
        } catch (final CustomisationException e) {
            assertThat(e.getStatus(), is(Status.IO_ERROR));
        }
    }

    @Test
    public void testOverwriting() {
        final MultipartFile file1 = new MockMultipartFile("file", "logo.png", "image/png", "foo".getBytes());

        try {
            customisationService.saveLogo(file1, false);
        } catch (final CustomisationException e) {
            fail("File should not exist yet");
        }

        final MultipartFile file2 = new MockMultipartFile("file", "logo.png", "image/png", "barr".getBytes());

        try {
            customisationService.saveLogo(file2, false);

            fail();
        } catch (final CustomisationException e) {
            assertThat(e.getStatus(), is(Status.FILE_EXISTS));
        }

        try {
            customisationService.saveLogo(file2, true);
        } catch (final CustomisationException e) {
            fail("File should have been overwritten");
        }

        assertThat(customisationService.getLogo("logo.png").length(), is(4L));
    }

}