package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Platform;

public class IndexDetailsPageITCase extends HostedTestBase {

    private IndexService indexService;
    private IndexesPage indexesPage;
    private IndexesDetailPage indexesDetailPage;

    public IndexDetailsPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        indexService = getApplication().createIndexService(getElementFactory());
    }

    @After
    public void tearDown(){
        indexService.deleteAllIndexes();
    }
}
