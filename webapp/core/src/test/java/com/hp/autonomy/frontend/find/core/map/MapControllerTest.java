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

    @Test(expected = IllegalArgumentException.class)
    public void getMapDataInvalidCallback() {
        controller.getMapData(URL, "0 BAD 0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMapDataInvalidUrl() {
        controller.getMapData("http://bad", "fn");
    }

    @Test
    public void onError() {
        final String badUrl = "htp://bad";
        when(config.getMap()).thenReturn(new MapConfiguration(badUrl, false, null, null, null, null));
        assertEquals("fn(\"error:Application error\")", controller.getMapData(badUrl, "fn"));
    }
}
