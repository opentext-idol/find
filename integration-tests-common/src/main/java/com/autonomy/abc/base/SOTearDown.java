package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.base.TearDown;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.autonomy.abc.selenium.keywords.KeywordFilter;

public enum SOTearDown implements TearDown<HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?>> {
    KEYWORDS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?> test) {
            test.getApplication().keywordService().deleteAll(KeywordFilter.ALL);
        }
    },
    PROMOTIONS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?> test) {
            test.getApplication().promotionService().deleteAll();
        }
    },
    USERS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?> test) {
            LoginService service = test.getApplication().loginService();
            if (service.getCurrentUser() == null) {
                service.login(test.getInitialUser());
            } else if (!service.getCurrentUser().equals(test.getInitialUser())) {
                service.logout();
                service.login(test.getInitialUser());
            }
            test.getApplication().userService().deleteOtherUsers();
        }
    };

    abstract void tearDownSafely(HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?> test);

    @Override
    public void tearDown(HybridAppTestBase<? extends SearchOptimizerApplication<?>, ?> test) {
        if (test.hasSetUp()) {
            tearDownSafely(test);
        }
    }
}
