package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

public class IdolConfigurationTest extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private IdolConfiguration idolConfiguration;

    @Test
    public void wiring() {
        assertNotNull(idolConfiguration.aciService());
    }

    @Test
    public void idolResponseParser() {
        assertNotNull(idolConfiguration.aciService());
    }
}
