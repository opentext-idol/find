/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.applications;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CustomApplicationsConfigTest extends ConfigurationComponentTest<CustomApplication> {
    private final String APP_NAME = "Some app name";
    private final String EXAMPLE_URL = "http://some.url.com";
    private CustomApplicationsConfig config;

    @Before
    public void setUp() {
        super.setUp();
        config = constructConfig(APP_NAME, EXAMPLE_URL);
    }

    @Override
    protected Class<CustomApplication> getType() {
        return CustomApplication.class;
    }

    @Override
    protected CustomApplication constructComponent() {
        return CustomApplication.builder()
                .applicationName(APP_NAME)
                .url(EXAMPLE_URL)
                .icon("hp-app")
                .openInNewTab(false)
                .enabled(false)
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/applications/customApplication.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<CustomApplication> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.applicationName", "Application name")
                .hasJsonPathStringValue("$.url", "http://example.url.com")
                .hasJsonPathStringValue("$.icon", "hp-monitor")
                .hasJsonPathBooleanValue("$.openInNewTab", true)
                .hasJsonPathBooleanValue("$.enabled", true);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<CustomApplication> objectContent) {
        objectContent.assertThat().isEqualTo(
            CustomApplication.builder()
                        .applicationName("Application name")
                        .url("http://example.url.com")
                        .icon("hp-monitor")
                        .openInNewTab(true)
                        .enabled(true)
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<CustomApplication> objectContent) {
        objectContent.assertThat().isEqualTo(
            CustomApplication.builder()
                        .applicationName(APP_NAME)
                        .url(EXAMPLE_URL)
                        .icon("hp-app")
                        .openInNewTab(false)
                        .enabled(false)
                        .build());
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, allOf(containsString("applicationName"), containsString("url")));
    }

    @Test
    public void testBasicValidateAcceptsAValidConfig() {
        try {
            config.basicValidate(null);
        } catch(ConfigException e) {
            fail("A valid configuration should not throw an exception.");
        }
    }

    @Test
    public void testBasicValidateRejectsEmptyAppName() {
        config = constructConfig("", EXAMPLE_URL);
        validateWithException("The application name must be a non-empty string, e.g. \"IDOL Admin\".");
    }

    @Test
    public void testBasicValidateRejectsNullAppName() {
        config = constructConfig(null, EXAMPLE_URL);
        validateWithException("The application name must be a non-empty string, e.g. \"IDOL Admin\".");
    }

    @Test
    public void testBasicValidateRejectsInvalidUrl() {
        config = constructConfig(APP_NAME, "http://nonsuch:12312312312314a///");
        validateWithException("The URL provided for \"" + APP_NAME + "\" is malformed.");
    }

    @Test
    public void testBasicValidateAllowsRelativeUrl() {
        config = constructConfig(APP_NAME, "static-HEAD/html/idol-saved-search-status.html");
    }

    @Test
    public void testBasicValidateRejectsNullUrl() {
        config = constructConfig(APP_NAME, null);
        validateWithException("The \"url\" property for \"" + APP_NAME + "\" must not be empty.");
    }

    @Test
    public void testBasicValidateWorksEvenWhenAppIsDisabled() {
        config = CustomApplicationsConfig.builder()
                .application(
                    CustomApplication.builder()
                                .enabled(false)
                                .build()
                )
                .build();

        validateWithException("The application name must be a non-empty string, e.g. \"IDOL Admin\".");
    }

    @Test
    public void testOverridingDefaultsWorks() {
        config = CustomApplicationsConfig.builder()
                .application(
                    CustomApplication.builder()
                                .applicationName(APP_NAME)
                                .url(EXAMPLE_URL)
                                .icon("hp-monitor")
                                .openInNewTab(true)
                                .enabled(false)
                                .build()
                )
                .build();

        final CustomApplication app = config.getApplications().iterator().next();
        assertEquals(APP_NAME, app.getApplicationName());
        assertEquals(EXAMPLE_URL, app.getUrl());
        assertEquals("hp-monitor", app.getIcon());
        assertEquals(true, app.isOpenInNewTab());
        assertEquals(false, app.getEnabled());
    }

    @Test
    public void testEnabledDefaultsToTrue() {
        assertEquals(true, config.getApplications().iterator().next().getEnabled());
    }

    @Test
    public void testOpenInNewTabDefaultsToFalse() {
        assertEquals(false, config.getApplications().iterator().next().isOpenInNewTab());
    }

    @Test
    public void testIconDefaultsToEmptyString() {
        assertEquals("", config.getApplications().iterator().next().getIcon());
    }

    private void validateWithException(final String expected) {
        try {
            config.basicValidate(null);
            fail("An exception should have been thrown");
        } catch(ConfigException e) {
            assertThat(e.getMessage(), containsString(expected));
        }
    }

    private CustomApplicationsConfig constructConfig(final String name, final String url) {
        return CustomApplicationsConfig.builder()
                .applications(
                        Collections.singletonList(
                            CustomApplication.builder()
                                        //Do not set optional values here to check default behaviour
                                        .applicationName(name)
                                        .url(url)
                                        .build()
                        )
                )
                .build();
    }
}
