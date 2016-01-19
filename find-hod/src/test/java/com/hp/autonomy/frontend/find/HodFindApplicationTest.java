/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HodFindApplication.class)
@WebIntegrationTest({"server.port=0", "hp.find.persistentState = INMEMORY", "application.buildNumber=test"})
public class HodFindApplicationTest {
    @BeforeClass
    public static void init() {
        System.setProperty("hp.find.home", "./src/test/resources");
    }

    @Test
    public void contextLoads() {
    }
}
