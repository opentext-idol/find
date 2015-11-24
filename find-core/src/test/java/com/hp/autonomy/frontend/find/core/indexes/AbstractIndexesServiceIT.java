package com.hp.autonomy.frontend.find.core.indexes;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.types.IdolDatabase;
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

import static org.junit.Assert.assertFalse;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DispatcherServletConfiguration.class, AppConfiguration.class})
@TestPropertySource(properties = {"hp.find.persistentState = INMEMORY", "hp.find.home = ./target/test", "find.https.proxyHost = web-proxy.sdc.hpecorp.net", "find.https.proxyPort: 8080", "find.iod.api = https://api.havenondemand.com", "find.hod.sso = https://dev.havenondemand.com/sso.html"})
public abstract class AbstractIndexesServiceIT {
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
    protected ListIndexesController listIndexesController;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void noExcludedIndexes() throws Exception {
        final List<? extends IdolDatabase> databases = listIndexesController.listActiveIndexes();
        assertFalse(databases.isEmpty());
    }
}
