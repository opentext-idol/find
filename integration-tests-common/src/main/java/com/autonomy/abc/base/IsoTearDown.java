package com.autonomy.abc.base;

import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.fixtures.PromotionTearDownStrategy;
import com.autonomy.abc.fixtures.UserTearDownStrategy;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.base.TearDown;

public enum IsoTearDown implements TearDown<HybridAppTestBase<? extends IsoApplication<?>, ?>> {
    KEYWORDS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
            new KeywordTearDownStrategy().tearDown(test);
        }
    },
    PROMOTIONS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
            new PromotionTearDownStrategy().tearDown(test);
        }
    },
    USERS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
            new UserTearDownStrategy(test.getInitialUser()).tearDown(test);
        }
    };

    abstract void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test);

    @Override
    public void tearDown(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
        if (test.hasSetUp()) {
            tearDownSafely(test);
        }
    }
}
