package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.IsoApplication;
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
