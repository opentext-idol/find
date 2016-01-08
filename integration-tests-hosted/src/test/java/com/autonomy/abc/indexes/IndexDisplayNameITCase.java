package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexDisplayNameITCase extends HostedTestBase {
    public IndexDisplayNameITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    private IndexService indexService;
    private IndexesPage indexesPage;
    private Index testIndex;

    @Before
    public void setUp(){
        indexService = getApplication().createIndexService(getElementFactory());
        testIndex = new Index("thisisabittooyobbish5me","D1spl4y Nam3 AbC 123");
        indexesPage = indexService.setUpIndex(testIndex)
    }

    @After
    public void tearDown(){
        indexService.deleteAllIndexes();
    }
}
