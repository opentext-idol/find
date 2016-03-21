package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.base.TearDown;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.autonomy.abc.selenium.keywords.KeywordFilter;

public enum SOTearDown implements TearDown<SOTestBase> {
    KEYWORDS {
        @Override
        void tearDownSafely(SOTestBase test) {
            test.getApplication().keywordService().deleteAll(KeywordFilter.ALL);
        }
    },
    PROMOTIONS {
        @Override
        void tearDownSafely(SOTestBase test) {
            test.getApplication().promotionService().deleteAll();
        }
    },
    USERS {
        @Override
        void tearDownSafely(SOTestBase test) {
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

    abstract void tearDownSafely(SOTestBase test);

    @Override
    public void tearDown(SOTestBase test) {
        if (test.hasSetUp()) {
            tearDownSafely(test);
        }
    }
}
