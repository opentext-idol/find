/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "idol.find.persistentState = INMEMORY",
        "application.buildNumber=test",
        "mock.configuration=false",
        "spring.datasource.url = jdbc:h2:mem:find-db;DB_CLOSE_ON_EXIT=FALSE"
}, webEnvironment = WebEnvironment.RANDOM_PORT)
public class IdolFindApplicationTest {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("idol.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
    }

    @AfterClass
    public static void destroy() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Test
    public void contextLoads() {}
}
