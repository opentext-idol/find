package com.autonomy.abc.fixtures;

import com.autonomy.abc.base.HybridAppTestBase;
import com.hp.autonomy.frontend.selenium.base.TearDown;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.NullAuthenticationStrategy;
import org.openqa.selenium.TimeoutException;
import org.slf4j.LoggerFactory;

public class EmailTearDownStrategy implements TearDown<HybridAppTestBase<?, ?>> {
    private final Session session;
    private final AuthenticationStrategy strategy;

    public EmailTearDownStrategy(Session session, AuthenticationStrategy strategy) {
        this.session = session;
        this.strategy = strategy;
    }

    @Override
    public void tearDown(HybridAppTestBase<?, ?> test) {
        if (shouldTearDown(test)) {
            Window firstWindow = session.getActiveWindow();
            Window secondWindow = session.openWindow("about:blank");
            try {
                strategy.cleanUp(session.getDriver());
            } catch (TimeoutException e) {
                LoggerFactory.getLogger(EmailTearDownStrategy.class).warn("Could not tear down");
            } finally {
                secondWindow.close();
                firstWindow.activate();
            }
        }
    }

    private boolean shouldTearDown(HybridAppTestBase<?, ?> test) {
        return test.hasSetUp() && !strategy.equals(NullAuthenticationStrategy.getInstance());
    }
}
