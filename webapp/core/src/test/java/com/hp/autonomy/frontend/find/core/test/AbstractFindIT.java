/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SuppressWarnings({"UtilityClass", "SpringJavaAutowiredMembersInspection"})
@RunWith(SpringRunner.class)
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(properties = {
        "application.buildNumber=test",
        "idol.find.persistentState = INMEMORY",
        "idol.find.home = ./target/test",
        "find.https.proxyHost = web-proxy.sdc.hpecorp.net",
        "find.https.proxyPort: 8080",
        "find.https.proxyHost = web-proxy.sdc.hpecorp.net",
        "find.https.proxyPort: 8080",
        "spring.datasource.url = jdbc:h2:mem:find-db;DB_CLOSE_ON_EXIT=FALSE",
        "mock.authenticationRetriever=false",
        "find.metrics.enabled=true"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/add-user.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean-database.sql")
})
public abstract class AbstractFindIT {
    protected static final String TEST_DIR = "./target/test";
    @Autowired
    protected WebApplicationContext wac;
    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;
    protected MockMvc mockMvc;

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

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    protected Authentication userAuth() {
        return mvcIntegrationTestUtils.userAuth();
    }

    protected Authentication biAuth() {
        return mvcIntegrationTestUtils.biAuth();
    }

    protected Authentication adminAuth() {
        return mvcIntegrationTestUtils.adminAuth();
    }
}
