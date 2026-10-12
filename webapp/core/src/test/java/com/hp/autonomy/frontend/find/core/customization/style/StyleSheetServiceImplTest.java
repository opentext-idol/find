/*
 * Copyright 2026 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.customization.style;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Compiles the real stylesheets against the real classpath - picks up resources that core should package.
 */
@RunWith(MockitoJUnitRunner.class)
public class StyleSheetServiceImplTest {
    @Mock private ConfigService<StyleConfiguration> configService;
    private StyleSheetServiceImpl styleSheetService;

    @Before
    public void setUp() throws Exception {
        final StyleConfiguration configuration = StyleConfiguration.builder()
            .termHighlightColor("#ffffff")
            .termHighlightBackground("#000000")
            .build();
        Mockito.doReturn(configuration).when(configService).getConfig();

        styleSheetService = new StyleSheetServiceImpl(configService, "HEAD");
        styleSheetService.generateCss();
    }

    @Test
    public void testAppStyleSheet() throws Exception {
        assertCompiled("app.css");
    }

    @Test
    public void testLoginStyleSheet() throws Exception {
        assertCompiled("login.css");
    }

    private void assertCompiled(final String fileName) {
        final Optional<StyleSheet> styleSheet = styleSheetService.getCss(fileName);
        Assert.assertTrue(fileName + " was not generated", styleSheet.isPresent());
        Assert.assertFalse(fileName + " is empty", styleSheet.get().getStyleSheet().isBlank());
    }

}
