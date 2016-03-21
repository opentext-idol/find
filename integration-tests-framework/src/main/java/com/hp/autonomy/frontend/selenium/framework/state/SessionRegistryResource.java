package com.hp.autonomy.frontend.selenium.framework.state;

import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.control.Session;
import org.junit.rules.ExternalResource;

public class SessionRegistryResource extends ExternalResource {
    private final SeleniumTest<?, ?> test;

    public SessionRegistryResource(SeleniumTest<?, ?> test) {
        this.test = test;
    }

    @Override
    protected void before() throws Throwable {
        Session session  = test.getSessionRegistry().startSession();
        test.getApplication().inWindow(session.getActiveWindow());
    }

    @Override
    protected void after() {
        test.getSessionRegistry().clear();
    }
}
