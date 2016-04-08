package com.autonomy.abc.fixtures;

import com.autonomy.abc.base.HostedTestBase;
import com.hp.autonomy.frontend.selenium.base.TearDown;

public class IndexTearDownStrategy implements TearDown<HostedTestBase> {
    @Override
    public void tearDown(HostedTestBase test) {
        if (test.hasSetUp()) {
            test.getApplication().connectionService().deleteAllConnections(true);
            test.getApplication().indexService().deleteAllIndexes();
        }
    }
}
