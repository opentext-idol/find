package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.hp.autonomy.frontend.selenium.base.TearDown;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.autonomy.abc.selenium.keywords.KeywordFilter;

public enum IsoTearDown implements TearDown<HybridAppTestBase<? extends IsoApplication<?>, ?>> {
    KEYWORDS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
            test.getApplication().keywordService().deleteAll(KeywordFilter.ALL);
        }
    },
    PROMOTIONS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
            test.getApplication().promotionService().deleteAll();
        }
    },
    USERS {
        @Override
        void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
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

    abstract void tearDownSafely(HybridAppTestBase<? extends IsoApplication<?>, ?> test);

    @Override
    public void tearDown(HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
        if (test.hasSetUp()) {
            tearDownSafely(test);
        }
    }
}
