package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FlywayIdolConfigUpdateHandlerTest {
    @MockitoBean
    private Flyway flyway;
    @Mock private IdolFindConfig config;
    private FlywayIdolConfigUpdateHandler flywayIdolConfigUpdateHandler;

    @Before
    public void setUp() {
        flywayIdolConfigUpdateHandler = new FlywayIdolConfigUpdateHandler(flyway);
    }

    @Test
    public void updateWithValidConfig() {
        flywayIdolConfigUpdateHandler.update(config);
        Mockito.verify(flyway).repair();
        Mockito.verify(flyway).migrate();
    }

}
