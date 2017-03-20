package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.test.MockConfig;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExportConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ExportConfigurationTest {
    @MockBean
    private ConfigService<MockConfig> configService;
    @Autowired
    private PowerPointService powerPointService;
    @Mock
    private MockConfig config;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void powerpointService() throws TemplateLoadException {
        assertNotNull(powerPointService);
        powerPointService.validateTemplate();
    }
}
