package com.autonomy.abc.config;

import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.application.LoginService;

public enum ABCTearDown implements TearDown<ABCTestBase> {
    KEYWORDS {
        @Override
        void tearDownSafely(ABCTestBase test) {
            test.getApplication().keywordService().deleteAll(KeywordFilter.ALL);
        }
    },
    PROMOTIONS {
        @Override
        void tearDownSafely(ABCTestBase test) {
            test.getApplication().promotionService().deleteAll();
        }
    },
    USERS {
        @Override
        void tearDownSafely(ABCTestBase test) {
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

    abstract void tearDownSafely(ABCTestBase test);

    @Override
    public void tearDown(ABCTestBase test) {
        if (test.hasSetUp()) {
            tearDownSafely(test);
        }
    }
}
