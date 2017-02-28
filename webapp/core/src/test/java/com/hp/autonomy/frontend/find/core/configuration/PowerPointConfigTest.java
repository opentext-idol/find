/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.reports.powerpoint.dto.Anchor;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PowerPointConfigTest {
    private PowerPointConfig powerPointConfig;
    private static final double EPSILON = 1e-10;

    @Before
    public void setUp() {
        powerPointConfig = new PowerPointConfig.Builder()
                .setMarginTop(0.0)
                .setMarginBottom(0.0)
                .setMarginLeft(0.0)
                .setMarginRight(0.0)
                .setTemplateFile(null)
                .build();
    }

    @Test
    public void merge() {
        assertEquals(powerPointConfig, new PowerPointConfig.Builder().build().merge(powerPointConfig));
    }

    @Test
    public void mergeNoDefaults() {
        assertEquals(powerPointConfig, powerPointConfig.merge(null));
    }

    @Test
    public void basicValidate() throws ConfigException {
        powerPointConfig.basicValidate(null);

        final Anchor anchor = powerPointConfig.getAnchor();
        assertEquals("Wrong left edge", anchor.getX(), 0, EPSILON);
        assertEquals("Wrong top edge", anchor.getY(), 0, EPSILON);
        assertEquals("Wrong width", anchor.getWidth(), 1, EPSILON);
        assertEquals("Wrong height", anchor.getHeight(), 1, EPSILON);
    }


    @Test
    public void basicValidateAnchors() throws ConfigException {
        final PowerPointConfig config = new PowerPointConfig.Builder()
                .setMarginTop(0.01)
                .setMarginBottom(0.02)
                .setMarginLeft(0.03)
                .setMarginRight(0.04)
                .build();

        config.basicValidate(null);

        final Anchor anchor = config.getAnchor();
        assertEquals("Wrong left edge", anchor.getX(), 0.03, EPSILON);
        assertEquals("Wrong top edge", anchor.getY(), 0.01, EPSILON);
        assertEquals("Wrong width", anchor.getWidth(), 0.93, EPSILON);
        assertEquals("Wrong height", anchor.getHeight(), 0.97, EPSILON);
    }

    @Test(expected = ConfigException.class)
    public void basicValidateInvalidTopBottom() throws ConfigException {
        new PowerPointConfig.Builder()
                .setMarginBottom(0.4)
                .setMarginTop(0.7)
                .build()
                .basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void basicValidateInvalidLeftRight() throws ConfigException {
        new PowerPointConfig.Builder()
                .setMarginLeft(0.5)
                .setMarginRight(0.5)
                .build()
                .basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void basicValidateInvalidTemplateFilePath() throws ConfigException {
        new PowerPointConfig.Builder()
                .setTemplateFile("no/such/file.exists")
                .build()
                .basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void basicValidateBlankFile() throws ConfigException, IOException {
        final File tmpFile = File.createTempFile("temp", "pptx");
        tmpFile.deleteOnExit();

        new PowerPointConfig.Builder()
                .setTemplateFile(tmpFile.getAbsolutePath())
                .build()
                .basicValidate(null);
    }
}
