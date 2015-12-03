/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotNull;

@TestPropertySource(properties = "hp.find.backend = IDOL")
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
