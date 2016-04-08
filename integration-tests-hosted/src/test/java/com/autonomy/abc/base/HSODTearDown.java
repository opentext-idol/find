package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.base.TearDown;

public enum HSODTearDown implements TearDown<HostedTestBase> {
    INDEXES {
        @Override
        public void tearDown(HostedTestBase test) {
            new IndexTearDownStrategy().tearDown(test);
        }
    },
}
