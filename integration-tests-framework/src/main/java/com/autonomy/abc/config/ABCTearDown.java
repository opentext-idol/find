package com.autonomy.abc.config;

import com.autonomy.abc.selenium.keywords.KeywordFilter;

public enum ABCTearDown implements TearDown<ABCTestBase> {
    KEYWORDS {
        @Override
        public void tearDown(ABCTestBase test) {
            if (test.hasSetUp()) {
                test.getApplication().keywordService().deleteAll(KeywordFilter.ALL);
            }
        }
    },
    PROMOTIONS {
        @Override
        public void tearDown(ABCTestBase test) {
            if (test.hasSetUp()) {
                test.getApplication().promotionService().deleteAll();
            }
        }
    };

    @Override
    public abstract void tearDown(ABCTestBase test);
}
