package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.databases.Database;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.AppConfiguration;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfiguration.class)
@TestPropertySource(properties = {"hp.find.persistentState = INMEMORY", "hp.find.home = ./target/test", "find.https.proxyHost = web-proxy.sdc.hpecorp.net", "find.https.proxyPort: 8080", "find.iod.api = https://api.havenondemand.com", "find.hod.sso = https://dev.havenondemand.com/sso.html"})
public class HodIndexesServiceIT {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("hp.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
        FileUtils.copyFileToDirectory(new File("./src/test/resources/config.json"), directory);
    }

    @AfterClass
    public static void destroy() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Autowired
    private HodIndexesService hodIndexesService;

    @Autowired
    private TokenProxy<EntityType.Application, TokenType.Simple> tokenProxy;

    @Test
    public void noExcludedIndexes() throws HodErrorException {
        assertTrue(hodIndexesService.listActiveIndexes().isEmpty());

        final List<Database> databases = hodIndexesService.listVisibleIndexes();
        assertFalse(databases.isEmpty());

        final Resources resources = hodIndexesService.listIndexes(tokenProxy);
        assertEquals(1, resources.getResources().size()); //we should only have the default index
        assertFalse(resources.getPublicResources().isEmpty());
        assertEquals(resources.getResources().size() + resources.getPublicResources().size(), databases.size());
    }
}
