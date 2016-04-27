package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class FindTestBase extends HybridAppTestBase<FindApplication<?>, FindElementFactory> {
    protected FindTestBase(TestConfig config) {
        super(config, FindApplication.ofType(config.getType()));
    }

    protected final User getCurrentUser() {
        return getApplication().loginService().getCurrentUser();
    }

}
