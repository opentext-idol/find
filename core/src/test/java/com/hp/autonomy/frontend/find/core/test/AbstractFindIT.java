/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SuppressWarnings("UtilityClass")
@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest({
        "application.buildNumber=test",
        "server.port=0",
        "hp.find.persistentState = INMEMORY",
        "hp.find.home = ./target/test",
        "find.https.proxyHost = web-proxy.sdc.hpecorp.net",
        "find.https.proxyPort: 8080",
        "hp.find.home = ./target/test",
        "find.https.proxyHost = web-proxy.sdc.hpecorp.net",
        "find.https.proxyPort: 8080",
        "find.iod.api = https://api.havenondemand.com",
        "find.hod.sso = https://dev.havenondemand.com/sso.html",
        "spring.datasource.url = jdbc:h2:mem:find-db;DB_CLOSE_ON_EXIT=FALSE",
        "mock.authenticationRetriever=false"
})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean-database.sql")
public abstract class AbstractFindIT {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("hp.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
    }

    @AfterClass
    public static void destroy() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;

    protected MockMvc mockMvc;

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
