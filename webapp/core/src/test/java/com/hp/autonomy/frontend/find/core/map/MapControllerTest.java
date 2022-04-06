/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.map;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.core.test.MockConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Pattern;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MapController.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MapControllerTest {
    private static final String URL = "http://placehold.it/800x300";
    private static final Pattern EXPECTED_SUCCESS_PATTERN = Pattern.compile("fn\\(.+\\)");
    @MockBean
    private ConfigService<MockConfig> configService;
    @Autowired
    private MapController controller;
    @Mock
    private MockConfig config;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        when(config.getMap()).thenReturn(new MapConfiguration(URL, false, null, null, null, null));
    }

    @Test
    public void getMapData() {
        assertTrue(EXPECTED_SUCCESS_PATTERN.matcher(controller.getMapData(URL, "fn")).matches());
    }

    @Test
    public void getMapDataInvalidCallback() {
        try {
            controller.getMapData(URL, "0 BAD 0");
            fail("Exception should have been thrown");
        } catch(final IllegalArgumentException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       is("Invalid callback function name"));
        }
    }

    @Test
    public void getMapDataInvalidUrl() {
        try {
            controller.getMapData("http://bad", "fn");
            fail("Exception should have been thrown");
        } catch(final IllegalArgumentException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       is("We only allow proxying to the tile server"));
        }
    }

    @Test
    public void onError() {
        final String badUrl = "htp://bad";
        when(config.getMap()).thenReturn(new MapConfiguration(badUrl, false, null, null, null, null));
        assertEquals("fn(\"error:Application error\")", controller.getMapData(badUrl, "fn"));
    }
}
