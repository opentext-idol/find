package com.autonomy.abc.config;

public enum HSODTearDown implements TearDown<HostedTestBase> {
    CONNECTIONS {
        @Override
        public void tearDown(HostedTestBase test) {
            if (test.hasSetUp()) {
                test.getApplication().connectionService().deleteAllConnections(false);
            }
        }
    },
    INDEXES {
        @Override
        public void tearDown(HostedTestBase test) {
            if (test.hasSetUp()) {
                test.getApplication().connectionService().deleteAllConnections(true);
                test.getApplication().indexService().deleteAllIndexes();
            }
        }
    }
}
